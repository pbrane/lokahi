package org.opennms.horizon.minion.flows.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.horizon.minion.flows.adapter.common.Adapter;
import org.opennms.horizon.minion.flows.adapter.ipfix.IpfixAdapter;
import org.opennms.horizon.minion.flows.adapter.ipfix.IpfixAdapterFactory;
import org.opennms.horizon.minion.flows.adapter.netflow5.Netflow5Adapter;
import org.opennms.horizon.minion.flows.adapter.netflow5.Netflow5AdapterFactory;
import org.opennms.horizon.minion.flows.adapter.netflow9.Netflow9AdapterFactory;
import org.opennms.horizon.minion.flows.parser.flowmessage.NetflowVersion;
import org.opennms.horizon.shared.ipc.rpc.IpcIdentity;
import org.opennms.horizon.shared.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.horizon.shared.protobuf.util.ProtobufUtil;
import org.opennms.sink.flows.contract.FlowsConfig;

import com.google.common.io.Resources;
import com.google.protobuf.Any;


public class AdapterHolderTest {

    private TelemetryRegistry telemetryRegistry;

    @Before
    public void setUp() throws IOException {
        MessageDispatcherFactory messageDispatcherFactory = Mockito.mock(MessageDispatcherFactory.class);
        IpcIdentity identity = new IpcIdentity() {
            @Override
            public String getId() {
                return "test-id";
            }

            @Override
            public String getLocation() {
                return "test-location";
            }
        };
        telemetryRegistry = new TelemetryRegistryImpl(messageDispatcherFactory, identity, new ListenerHolder(), new AdapterHolder());
        new Netflow5AdapterFactory(telemetryRegistry);
        new Netflow9AdapterFactory(telemetryRegistry);
        new IpfixAdapterFactory(telemetryRegistry);
        ConfigManager manager = new ConfigManager(telemetryRegistry);
        manager.create(null, readFlowsConfig());
    }

    @Test
    public void testFindByNetflowVersion() {
        // Given
        NetflowVersion netflowVersion1 = NetflowVersion.IPFIX;
        NetflowVersion netflowVersion2 = NetflowVersion.V5;

        // When
        Adapter foundAdapter = telemetryRegistry.getAdapterHolder().findByNetflowVersion(netflowVersion1);

        // Then
        assertEquals(IpfixAdapter.class.getSimpleName(), foundAdapter.getClass().getSimpleName());

        // When
        foundAdapter = telemetryRegistry.getAdapterHolder().findByNetflowVersion(netflowVersion2);

        // Then
        assertEquals(Netflow5Adapter.class.getSimpleName(), foundAdapter.getClass().getSimpleName());

        // When
        telemetryRegistry.getAdapterHolder().clearAll();
        foundAdapter = telemetryRegistry.getAdapterHolder().findByNetflowVersion(netflowVersion2);

        // Then
        assertNull(foundAdapter);
    }

    Any readFlowsConfig() throws IOException {
        URL url = this.getClass().getResource("/flows-config.json");
        return Any.pack(ProtobufUtil.fromJson(Resources.toString(url, StandardCharsets.UTF_8), FlowsConfig.class));
    }
}
