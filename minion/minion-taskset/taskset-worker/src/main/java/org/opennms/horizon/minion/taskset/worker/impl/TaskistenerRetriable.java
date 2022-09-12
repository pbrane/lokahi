package org.opennms.horizon.minion.taskset.worker.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.resources.SpringResource;
import org.opennms.horizon.minion.plugin.api.registries.ListenerFactoryRegistry;
import org.opennms.horizon.minion.taskset.worker.RetriableExecutor;
import org.opennms.horizon.minion.taskset.worker.TaskExecutionResultProcessor;
import org.opennms.horizon.minion.plugin.api.Listener;
import org.opennms.horizon.minion.plugin.api.ListenerFactory;
import org.opennms.taskset.model.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core of the Workflow Executor for LISTENERS which implements the RetriableExecutor, focusing the logic for starting
 *  and maintaining the listener.  Used with WorkflowCommonRetryExecutor for retry handling.
 *
 * NOTE: there currently is no mechanism by which a LISTENER plugin can notify of a lost listener.  If there is a need
 *  to trigger retries, a way for the Listener to notify back of the failure must be added.
 */
public class TaskistenerRetriable implements RetriableExecutor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskistenerRetriable.class);

    @SpringResource(resourceClass = ListenerFactoryRegistry.class)
    private transient ListenerFactoryRegistry listenerFactoryRegistry;

    private Logger log = DEFAULT_LOGGER;

    private TaskDefinition workflow;
    private Listener listener;
    private TaskExecutionResultProcessor resultProcessor;

    private Runnable onDisconnect;

    public TaskistenerRetriable(TaskDefinition workflow, TaskExecutionResultProcessor resultProcessor) {
        this.workflow = workflow;
        this.resultProcessor = resultProcessor;
    }

//========================================
// API
//----------------------------------------

    @Override
    public void init(Runnable handleRetryNeeded) {
        onDisconnect = handleRetryNeeded;
    }

    @Override
    public void attempt() throws Exception {
        ListenerFactory listenerFactory = lookupListenerFactory(workflow);

        if (listenerFactory != null) {
            log.info("Staring listener: plugin-name={}; workflow-id={}", workflow.getPluginName(), workflow.getId());

            Map<String, Object> castMap = new HashMap<>(workflow.getParameters());

            listener = listenerFactory.create(
                    serviceMonitorResponse -> resultProcessor.queueSendResult(workflow.getId(),
                            serviceMonitorResponse), castMap);
            listener.start();
        } else {
            log.warn("Listener plugin not registered; workflow will not run: plugin-name={}; workflow-id={}",
                    workflow.getPluginName(), workflow.getId());

            throw new Exception("Listener plugin not registered: plugin-name=" + workflow.getPluginName());
        }
    }

    @Override
    public void cancel() {
        if (listener != null) {
            listener.stop();
        }
    }


//========================================
// Setup Internals
//----------------------------------------
    private ListenerFactory lookupListenerFactory(TaskDefinition workflow) {
        String pluginName = workflow.getPluginName();

        ListenerFactory result = listenerFactoryRegistry.getService(pluginName);

        return result;
    }
}
