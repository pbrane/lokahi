/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.horizon.minion.taskset.worker.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorRequest;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.horizon.minion.scheduler.OpennmsScheduler;
import org.opennms.horizon.minion.taskset.worker.TaskExecutionResultProcessor;
import org.opennms.horizon.minion.taskset.worker.TaskExecutorLocalService;
import org.opennms.horizon.shared.logging.Logging;
import org.opennms.taskset.contract.MonitorSetConfig;
import org.opennms.taskset.contract.TaskDefinition;
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
    private static final String LOG_PREFIX = "monitor";

    private TaskDefinition taskDefinition;
    private OpennmsScheduler scheduler;
    private TaskExecutionResultProcessor resultProcessor;
    private MonitorRegistry monitorRegistry;
    private ExecutorService executor;
    private AtomicBoolean active = new AtomicBoolean(false);

    public TaskExecutorLocalMonitorServiceImpl(
            OpennmsScheduler scheduler,
            TaskDefinition taskDefinition,
            TaskExecutionResultProcessor resultProcessor,
            MonitorRegistry monitorRegistry,
            ExecutorService executor) {
        this.taskDefinition = taskDefinition;
        this.scheduler = scheduler;
        this.resultProcessor = resultProcessor;
        this.monitorRegistry = monitorRegistry;
        this.executor = executor;
    }

    // ========================================
    // API
    // ----------------------------------------

    @Override
    public void start() throws Exception {
        try {
            String whenSpec = taskDefinition.getSchedule().trim();

            // If the value is all digits, use it as periodic time in milliseconds
            if (whenSpec.matches("^\\d+$")) {
                long period = Long.parseLong(taskDefinition.getSchedule());

                scheduler.schedulePeriodically(
                        taskDefinition.getId(), period, TimeUnit.MILLISECONDS, this::executeSerializedIteration);
            } else {
                // Not a number, REQUIRED to be a CRON expression
                scheduler.scheduleTaskOnCron(taskDefinition.getId(), whenSpec, this::executeSerializedIteration);
            }

        } catch (Exception exc) {
            // TODO: throttle - we can get very large numbers of these in a short time
            if (log.isDebugEnabled()) {
                log.debug("error starting workflow {}", taskDefinition.getId(), exc);
            } else {
                log.warn("error starting workflow {}, message {}", taskDefinition.getId(), exc.getMessage());
            }
        }
    }

    @Override
    public void cancel() {
        scheduler.cancelTask(taskDefinition.getId());
    }

    // ========================================
    // Processing
    // ----------------------------------------

    private void executeSerializedIteration() {
        // Verify it's not already active
        if (active.compareAndSet(false, true)) {
            log.trace("Executing iteration of task: workflow-uuid={}", taskDefinition.getId());
            try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(LOG_PREFIX)) {
                executeIteration();
            }
        } else {
            log.debug(
                    "Skipping iteration of task as prior iteration is still active: workflow-uuid={}",
                    taskDefinition.getId());
        }
    }

    private void executeIteration() {
        final MonitorSetConfig monitors;
        try {
            monitors = this.taskDefinition.getConfiguration().unpack(MonitorSetConfig.class);
        } catch (final InvalidProtocolBufferException e) {
            log.error("Failed to unpack monitor task configuration", e);
            return;
        }

        final var futures = new ArrayList<CompletableFuture<ServiceMonitorResponse>>();
        for (final var config : monitors.getMonitorConfigList()) {
            final var monitorManager = monitorRegistry.getService(config.getMonitorType());
            if (monitorManager == null) {
                log.warn("Skipping service monitor execution; monitor not found: {}", config.getMonitorType());
                continue;
            }

            try {
                final var monitor = monitorManager.create();

                final var request = ServiceMonitorRequest.builder()
                        .taskId(this.taskDefinition.getId())
                        .monitoredEntityId(config.getMonitoredEntityId())
                        .monitorType(config.getMonitorType())
                        .configuration(config.getConfiguration())
                        .build();

                final var future =
                        monitor.poll(request.getConfiguration()).whenComplete(this.handleExecutionComplete(request));

                futures.add(future);

            } catch (Exception exc) {
                // TODO: throttle - we can get very large numbers of these in a short time
                if (log.isDebugEnabled()) {
                    log.debug("error executing workflow {}", taskDefinition.getId(), exc);
                } else {
                    log.warn("error executing workflow {} , message = {}", taskDefinition.getId(), exc.getMessage());
                }
            }
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).whenComplete((result, ex) -> {
            this.active.set(false);
        });
    }

    private BiConsumer<ServiceMonitorResponse, Throwable> handleExecutionComplete(
            final ServiceMonitorRequest serviceMonitorRequest) {
        return (ServiceMonitorResponse serviceMonitorResponse, Throwable exc) -> {
            if (exc == null) {
                resultProcessor.queueSendResult(taskDefinition, serviceMonitorRequest, serviceMonitorResponse);

            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "error executing workflow; workflow-uuid={}, monitor={}",
                            taskDefinition.getId(),
                            serviceMonitorRequest.getMonitoredEntityId(),
                            exc);
                } else {
                    log.warn(
                            "error executing workflow; workflow-uuid={}, monitor={}, message={}",
                            taskDefinition.getId(),
                            serviceMonitorRequest.getMonitoredEntityId(),
                            exc.getMessage());
                }
            }
        };
    }
}
