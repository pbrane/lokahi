package org.opennms.horizon.minion.flows.adapter.common;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opennms.horizon.grpc.telemetry.contract.TelemetryMessage;
import org.opennms.horizon.minion.flows.adapter.imported.ContextKey;
import org.opennms.horizon.minion.flows.adapter.imported.Flow;
import org.opennms.horizon.minion.flows.adapter.imported.FlowSource;
import org.opennms.horizon.minion.flows.parser.flowmessage.FlowMessage;

public class TelemetryMessageProtoCreatorTest {

    @Test
    public void testCreateMessage() {
        // Given
        FlowSource flowSource = new FlowSource("anylocation", "anysourceAddress", new ContextKey("context:key"));
        Flow flow1 = new NetflowMessage(FlowMessage.newBuilder().build(), Instant.now());
        Flow flow2 = new NetflowMessage(FlowMessage.newBuilder().build(), Instant.now().minusSeconds(10));
        List<Flow> flows = Arrays.asList(flow1, flow2);
        List<PackageDefinition> packageDefinitions = new ArrayList<>();
        // When
        TelemetryMessage telemetryMessage = TelemetryMessageProtoCreator.createMessage(flowSource,
            flows, true, true, packageDefinitions);

        // Then
        assertNotNull(telemetryMessage);
        assertNotNull(telemetryMessage.getBytes());
        assertTrue(telemetryMessage.getProcessingOptions().getApplicationThresholding());
        assertTrue(telemetryMessage.getProcessingOptions().getApplicationDataCollection());
        assertNotNull(telemetryMessage.getProcessingOptions().getPackageDefinitionList());
    }
}
