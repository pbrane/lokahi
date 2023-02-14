package org.opennms.horizon.minion.flows.adapter.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.horizon.minion.flows.adapter.Utils;
import org.opennms.horizon.minion.flows.adapter.imported.Flow;

import com.google.protobuf.ByteString;


public class TelemetryMessageProtoUtilTest {

    @Test
    public void testConvertToByteString() {
        // Given
        List<Flow> flows = Utils.getJsonFlowFromResources(Instant.now(),
            "/adapter/netflow9.json",
            "/adapter/netflow9_1.json");

        // When
        ByteString res = TelemetryMessageProtoUtil.convertToByteString(flows);

        // Then
        Assert.assertNotNull(res);
    }
}
