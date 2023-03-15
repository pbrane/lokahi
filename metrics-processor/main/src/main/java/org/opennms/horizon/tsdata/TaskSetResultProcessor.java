package org.opennms.horizon.tsdata;

import org.opennms.horizon.tsdata.collector.TaskSetCollectorResultProcessor;
import org.opennms.horizon.tsdata.detector.TaskSetDetectorResultProcessor;
import org.opennms.horizon.tsdata.monitor.TaskSetMonitorResultProcessor;
import org.opennms.taskset.contract.DetectorResponse;
import org.opennms.taskset.contract.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskSetResultProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSetResultProcessor.class);

    @Autowired
    private TaskSetDetectorResultProcessor taskSetDetectorResultProcessor;

    @Autowired
    private TaskSetMonitorResultProcessor taskSetMonitorResultProcessor;

    @Autowired
    private TaskSetCollectorResultProcessor taskSetCollectorResultProcessor;

    public void processTaskResult(String tenantId, TaskResult taskResult) {
        try {
            LOG.info("Processing task set result {}", taskResult);
            if (taskResult.hasMonitorResponse()) {
                LOG.info("Have monitor response, tenant-id: {}; task-id={};", tenantId, taskResult.getId());
                taskSetMonitorResultProcessor.processMonitorResponse(tenantId, taskResult, taskResult.getMonitorResponse());
            } else if (taskResult.hasDetectorResponse()) {
                DetectorResponse detectorResponse = taskResult.getDetectorResponse();
                taskSetDetectorResultProcessor.processDetectorResponse(tenantId, taskResult.getId(), detectorResponse);
            } else if (taskResult.hasCollectorResponse()) {
                taskSetCollectorResultProcessor.processCollectorResponse(tenantId, taskResult, taskResult.getCollectorResponse());
            }
        } catch (Exception exc) {
            // TODO: throttle
            LOG.warn("Error processing task result", exc);
        }
    }


}
