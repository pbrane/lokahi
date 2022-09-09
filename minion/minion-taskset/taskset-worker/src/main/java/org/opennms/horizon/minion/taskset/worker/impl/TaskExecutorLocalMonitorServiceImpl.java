package org.opennms.horizon.minion.taskset.worker.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.ignite.resources.SpringResource;
import org.opennms.horizon.minion.plugin.api.config.ConfigInjector;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.horizon.minion.taskset.worker.TaskExecutionResultProcessor;
import org.opennms.horizon.minion.taskset.worker.TaskExecutorLocalService;
import org.opennms.horizon.minion.plugin.api.MonitoredService;
import org.opennms.horizon.minion.plugin.api.ServiceMonitor;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorManager;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.scheduler.OpennmsScheduler;
import org.opennms.horizon.shared.utils.IPAddress;
import org.opennms.taskset.model.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local implementation of the service to execute a Monitor workflow.  This class runs "locally" only, so it is never
 *  serialized / deserialized; this enables the "ignite" service to be a thin implementation, reducing the chances of
 *  problems due to serialization/deserialization.
 */
public class TaskExecutorLocalMonitorServiceImpl implements TaskExecutorLocalService {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskExecutorLocalMonitorServiceImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private TaskDefinition taskDefinition;
    private OpennmsScheduler scheduler;
    private TaskExecutionResultProcessor resultProcessor;
    private final ConfigInjector pluginConfigInjector;
    private MonitorRegistry monitorRegistry;
    private ServiceMonitor monitor=null;

    private AtomicBoolean active = new AtomicBoolean(false);

    public TaskExecutorLocalMonitorServiceImpl(OpennmsScheduler scheduler, TaskDefinition taskDefinition,
        TaskExecutionResultProcessor resultProcessor, ConfigInjector pluginConfigInjector,
        MonitorRegistry monitorRegistry) {
        this.taskDefinition = taskDefinition;
        this.scheduler = scheduler;
        this.resultProcessor = resultProcessor;
        this.pluginConfigInjector = pluginConfigInjector;
        this.monitorRegistry = monitorRegistry;
    }

//========================================
// API
//----------------------------------------

    @Override
    public void start() throws Exception {
        try {
            String whenSpec = taskDefinition.getSchedule().trim();

            // If the value is all digits, use it as periodic time in milliseconds
            if (whenSpec.matches("^\\d+$")) {
                long period = Long.parseLong(taskDefinition.getSchedule());

                scheduler.schedulePeriodically(taskDefinition.getId(), period, TimeUnit.MILLISECONDS, this::executeSerializedIteration);
            } else {
                // Not a number, REQUIRED to be a CRON expression
                scheduler.scheduleTaskOnCron(taskDefinition.getId(), whenSpec, this::executeSerializedIteration);
            }

        } catch (Exception exc) {
            // TODO: throttle - we can get very large numbers of these in a short time
            log.warn("error starting workflow {}", taskDefinition.getId(), exc);
        }
    }

    @Override
    public void cancel() {
        scheduler.cancelTask(taskDefinition.getId());
    }


//========================================
// Setup Internals
//----------------------------------------
    private Optional<ServiceMonitor> lookupMonitor(TaskDefinition workflow) {
        String pluginName = workflow.getPluginName();

        ServiceMonitorManager result = monitorRegistry.getService(pluginName);

        if (result != null) {
            pluginConfigInjector.injectConfigs(result, workflow.getParameters());

            //TODO: what parameters (if any) to pass on creation? Probably none since we want to inject everything from schema.
            return Optional.of(result.create(null));
        }
        else return Optional.empty();
    }

//========================================
// Processing
//----------------------------------------

    private void executeSerializedIteration() {
        // Verify it's not already active
        if (active.compareAndSet(false, true)) {
            log.trace("Executing iteration of task: workflow-uuid={}", taskDefinition.getId());
            executeIteration();
        } else {
            log.debug("Skipping iteration of task as prior iteration is still active: workflow-uuid={}", taskDefinition.getId());
        }
    }

    private void executeIteration() {
        try {
            if (monitor == null) {
                Optional<ServiceMonitor> lazyMonitor = lookupMonitor(taskDefinition);
                if (lazyMonitor.isPresent()) {
                    this.monitor = lazyMonitor.get();
                }
            }
            if (monitor != null) {
                MonitoredService monitoredService = configureMonitoredService();

                Map<String, Object> castMap = new HashMap<>(taskDefinition.getParameters());
                CompletableFuture<ServiceMonitorResponse> future = monitor.poll(monitoredService, castMap);
                future.whenComplete(this::handleExecutionComplete);
            } else {
                log.info("Skipping service monitor execution; monitor not found: monitor=" + taskDefinition.getPluginName());
            }
        } catch (Exception exc) {
            // TODO: throttle - we can get very large numbers of these in a short time
            log.warn("error executing workflow " + taskDefinition.getId(), exc);
        }
    }

    private void handleExecutionComplete(ServiceMonitorResponse serviceMonitorResponse, Throwable exc) {
        log.trace("Completed execution: workflow-uuid={}", taskDefinition.getId());
        active.set(false);

        if (exc == null) {
            resultProcessor.queueSendResult(taskDefinition.getId(), serviceMonitorResponse);
        } else {
            log.warn("error executing workflow; workflow-uuid=" + taskDefinition.getId(), exc);
        }
    }

    private MonitoredService configureMonitoredService() throws UnknownHostException {
        String svcName = "TBD";
        String hostname = taskDefinition.getParameters().get("host");

        IPAddress ipAddress = lookupIpAddress(hostname);

        MonitoredService result = new GeneralMonitoredService(svcName, hostname, -1, "TBD", "TBD", ipAddress.toInetAddress());

        return result;
    }

    private IPAddress lookupIpAddress(String hostname) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(hostname);

        return new IPAddress(inetAddress);
    }
}
