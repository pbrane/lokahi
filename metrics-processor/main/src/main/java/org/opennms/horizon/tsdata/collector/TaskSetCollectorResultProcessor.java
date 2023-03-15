package org.opennms.horizon.tsdata.collector;

import org.opennms.taskset.contract.CollectorResponse;
import org.opennms.taskset.contract.MonitorType;
import org.opennms.taskset.contract.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TaskSetCollectorResultProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSetCollectorResultProcessor.class);

    @Autowired
    private TaskSetCollectorSnmpResponseProcessor taskSetCollectorSnmpResponseProcessor;

    @Autowired
    private TaskSetCollectorAzureResponseProcessor taskSetCollectorAzureResponseProcessor;

    public void processCollectorResponse(String tenantId, TaskResult taskResult, CollectorResponse collectorResponse) throws IOException {
        LOG.info("Have collector response, tenant-id: {}; task-id={};", tenantId, taskResult.getId());

        String[] labelValues =
            {
                collectorResponse.getIpAddress(),
                taskResult.getLocation(),
                taskResult.getSystemId(),
                collectorResponse.getMonitorType().name(),
                String.valueOf(collectorResponse.getNodeId())
            };

        if (collectorResponse.hasResult()) {
            MonitorType monitorType = collectorResponse.getMonitorType();
            if (monitorType.equals(MonitorType.SNMP)) {
                taskSetCollectorSnmpResponseProcessor.processSnmpCollectorResponse(tenantId, collectorResponse, labelValues);
            } else if (monitorType.equals(MonitorType.AZURE)) {
                taskSetCollectorAzureResponseProcessor.processAzureCollectorResponse(tenantId, collectorResponse, labelValues);
            } else {
                LOG.warn("Unrecognized monitor type");
            }
        } else {
            LOG.warn("No result in response");
        }
    }
}
