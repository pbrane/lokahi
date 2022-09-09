package org.opennms.horizon.minion.taskset.worker.impl;

import org.opennms.horizon.minion.plugin.api.config.ConfigInjector;
import org.opennms.horizon.minion.plugin.api.registries.MonitorRegistry;
import org.opennms.horizon.minion.taskset.worker.TaskExecutionResultProcessor;
import org.opennms.horizon.minion.taskset.worker.TaskExecutorLocalService;
import org.opennms.horizon.minion.taskset.worker.TaskExecutorLocalServiceFactory;
import org.opennms.horizon.minion.scheduler.OpennmsScheduler;
import org.opennms.taskset.model.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskExecutorLocalServiceFactoryImpl implements TaskExecutorLocalServiceFactory {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskExecutorLocalServiceFactoryImpl.class);

    private Logger log = DEFAULT_LOGGER;

    private final OpennmsScheduler scheduler;
    private final ConfigInjector pluginConfigInjector;
    private final TaskExecutionResultProcessor resultProcessor;

//========================================
// Constructor
//----------------------------------------

    public TaskExecutorLocalServiceFactoryImpl(
        OpennmsScheduler scheduler,
        TaskExecutionResultProcessor resultProcessor,
        ConfigInjector pluginConfigInjector) {

        this.scheduler = scheduler;
        this.resultProcessor = resultProcessor;
        this.pluginConfigInjector = pluginConfigInjector;
    }

//========================================
// API
//----------------------------------------

    @Override
    public TaskExecutorLocalService create(TaskDefinition taskDefinition, MonitorRegistry monitorRegistry) {
        switch (taskDefinition.getType()) {
            case MONITOR:
                return new TaskExecutorLocalMonitorServiceImpl(scheduler, taskDefinition, resultProcessor, pluginConfigInjector, monitorRegistry);

            case LISTENER:
                TaskistenerRetriable listenerService = new TaskistenerRetriable(taskDefinition, resultProcessor);
                return new TaskCommonRetryExecutor(scheduler, taskDefinition, resultProcessor, listenerService);

            case CONNECTOR:
                TaskConnectorRetriable connectorService = new TaskConnectorRetriable(taskDefinition, resultProcessor);
                return new TaskCommonRetryExecutor(scheduler, taskDefinition, resultProcessor, connectorService);

            default:
                throw new RuntimeException("unrecognized taskDefinition type " + taskDefinition.getType());
        }
    }
}
