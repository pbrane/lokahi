package org.opennms.horizon.minion.taskset.worker;

import org.opennms.horizon.minion.plugin.api.CollectionSet;
import org.opennms.horizon.minion.plugin.api.ScanResultsResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.taskset.contract.TaskContext;

public interface TaskExecutionResultProcessor {
    /**
     * Queue the given scan result to be sent out.
     *
     * @param uuid
     * @param scanResultsResponse
     */
    void queueSendResult(String uuid, TaskContext taskContext, ScanResultsResponse scanResultsResponse);


    /**
     * Queue the given monitor result to be sent out.
     *
     * @param uuid
     * @param serviceMonitorResponse
     */
    void queueSendResult(String uuid, TaskContext taskContext, ServiceMonitorResponse serviceMonitorResponse);


    void queueSendResult(String uuid, TaskContext taskContext, CollectionSet collectionSet);
}
