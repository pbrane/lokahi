package org.opennms.horizon.datachoices.dto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UsageStatisticsReportDTOTest {
    private static final String TEST_DEVICE_TYPE = "device";
    private static final int TEST_DEVICE_TYPE_COUNT = 1;
    private static final String TEST_SYSTEM_ID = "1234";
    private static final String TEST_VERSION = "1.0.0";
    private static final int TEST_NODE_COUNT = 1;
    private static final int TEST_MONITORED_SERVICES = 2;

    @Test
    public void testToJson() {
        UsageStatisticsReportDTO report = new UsageStatisticsReportDTO();
        report.setSystemId(TEST_SYSTEM_ID);
        report.setVersion(TEST_VERSION);
        report.setNodes(TEST_NODE_COUNT);
        report.setMonitoredServices(TEST_MONITORED_SERVICES);
        report.setDeviceTypeCounts(Collections.singletonMap(TEST_DEVICE_TYPE, TEST_DEVICE_TYPE_COUNT));

        String json = report.toJson();
        assertEquals("{\"systemId\":\"" + TEST_SYSTEM_ID + "\",\"version\":\"" + TEST_VERSION + "\"," + "\"nodes\":" + TEST_NODE_COUNT + "," +
            "\"monitoredServices\":" + TEST_MONITORED_SERVICES + ",\"deviceTypeCounts\":{\"" + TEST_DEVICE_TYPE + "\":" + TEST_DEVICE_TYPE_COUNT + "}}", json);
    }
}
