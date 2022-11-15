package org.opennms.horizon.inventory.grpc;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.inventory.InventoryApplication;
import org.opennms.horizon.inventory.PostgresInitializer;
import org.opennms.horizon.inventory.dto.DeviceCreateDTO;
import org.opennms.horizon.inventory.dto.DeviceServiceGrpc;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = InventoryApplication.class)
@ContextConfiguration(initializers = {PostgresInitializer.class})
class DeviceGrpcIntegrationTest extends GrpcTestBase {
    private DeviceServiceGrpc.DeviceServiceBlockingStub serviceStub;

    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private MonitoringLocationRepository monitoringLocationRepository;
    @Autowired
    private IpInterfaceRepository ipInterfaceRepository;

    public void initStub() {
        serviceStub = DeviceServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    public void cleanUp(){
        ipInterfaceRepository.deleteAll();
        nodeRepository.deleteAll();
        monitoringLocationRepository.deleteAll();

        channel.shutdown();
    }

    @Test
    void testCreateDevice() throws Exception {
        setupGrpc();
        initStub();

        String label = "label";

        DeviceCreateDTO createDTO = DeviceCreateDTO.newBuilder()
            .setLocation("location")
            .setLabel(label)
            .setManagementIp("127.0.0.1")
            .build();

        NodeDTO node = serviceStub.createDevice(createDTO);

        assertEquals(label, node.getNodeLabel());
    }

    @Test
    void testCreateDeviceMissingTenantId() throws Exception {
        setupGrpcWithOutTenantID();
        initStub();

        String label = "label";

        DeviceCreateDTO createDTO = DeviceCreateDTO.newBuilder()
            .setLocation("location")
            .setLabel(label)
            .setManagementIp("127.0.0.1")
            .build();

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, ()->serviceStub.createDevice(createDTO));
        Status status = StatusProto.fromThrowable(exception);
        assertThat(status.getCode()).isEqualTo(Code.UNAUTHENTICATED_VALUE);
        assertThat(status.getMessage()).isEqualTo("Missing tenant id");
    }

    @Test
    void testCreateDeviceBadIPAddress() throws Exception {
        setupGrpc();
        initStub();

        String label = "label";

        DeviceCreateDTO createDTO = DeviceCreateDTO.newBuilder()
            .setLocation("location")
            .setLabel(label)
            .setManagementIp("BAD")
            .build();

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, ()->serviceStub.createDevice(createDTO));
        Status status = StatusProto.fromThrowable(exception);
        assertThat(status.getCode()).isEqualTo(Code.INVALID_ARGUMENT_VALUE);
        assertThat(status.getMessage()).isEqualTo("Bad management_ip: BAD");
    }
}
