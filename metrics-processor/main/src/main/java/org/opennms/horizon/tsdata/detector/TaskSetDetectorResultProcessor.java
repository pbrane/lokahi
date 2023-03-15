package org.opennms.horizon.tsdata.detector;

import org.opennms.taskset.contract.DetectorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TaskSetDetectorResultProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSetDetectorResultProcessor.class);

    public void processDetectorResponse(String tenantId, String taskId, DetectorResponse detectorResponse) throws IOException {
            LOG.info("Have detector response, tenant-id: {}; task-id={}; detected={}", tenantId, taskId, detectorResponse.getDetected());
    }
}
