package org.opennms.horizon.inventory.grpc;

import com.google.protobuf.StringValue;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.inventory.InventoryApplication;
import org.opennms.horizon.inventory.dto.DeviceCreateDTO;
import org.opennms.horizon.inventory.dto.DeviceServiceGrpc;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = InventoryApplication.class)
@Testcontainers
public class DeviceGrpcIntegrationTest {

    private static final int grpcPort = 6768;
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.5-alpine")
        .withDatabaseName("inventory").withUsername("inventory")
        .withPassword("password").withExposedPorts(5432);

    @DynamicPropertySource
    private static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
            () -> String.format("jdbc:postgresql://localhost:%d/%s", postgres.getFirstMappedPort(), postgres.getDatabaseName()));
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("grpc.server.port", ()->grpcPort);
    }

    private ManagedChannel channel;
    private DeviceServiceGrpc.DeviceServiceBlockingStub serviceStub;

    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private MonitoringLocationRepository monitoringLocationRepository;
    @Autowired
    private IpInterfaceRepository ipInterfaceRepository;

    @BeforeEach
    public void prepare() {
        channel = ManagedChannelBuilder.forAddress("localhost", grpcPort)
            .usePlaintext().build();
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
        String label = "label";

        UUID tenant = new UUID(10, 12);
        DeviceCreateDTO createDTO = DeviceCreateDTO.newBuilder()
            .setLocation("location")
            .setTenantId(tenant.toString())
            .setLabel(label)
            .setManagementIp("127.0.0.1")
            .build();

        NodeDTO node = serviceStub.createDevice(createDTO);

        assertEquals(label, node.getNodeLabel());
    }

    @Test
    void testCreateDeviceBadUUID() throws Exception {
        String label = "label";

        DeviceCreateDTO createDTO = DeviceCreateDTO.newBuilder()
            .setLocation("location")
            .setTenantId("BAD")
            .setLabel(label)
            .setManagementIp("127.0.0.1")
            .build();

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, ()->serviceStub.createDevice(createDTO));
        Status status = StatusProto.fromThrowable(exception);
        assertThat(status.getCode()).isEqualTo(Code.INVALID_ARGUMENT_VALUE);
        assertThat(status.getMessage()).isEqualTo("Bad tenant_id: BAD");
    }

    @Test
    void testCreateDeviceBadIPAddress() throws Exception {
        String label = "label";

        UUID tenant = new UUID(10, 12);
        DeviceCreateDTO createDTO = DeviceCreateDTO.newBuilder()
            .setLocation("location")
            .setTenantId(tenant.toString())
            .setLabel(label)
            .setManagementIp("BAD")
            .build();

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, ()->serviceStub.createDevice(createDTO));
        Status status = StatusProto.fromThrowable(exception);
        assertThat(status.getCode()).isEqualTo(Code.INVALID_ARGUMENT_VALUE);
        assertThat(status.getMessage()).isEqualTo("Bad management_ip: BAD");
    }

    @Test
    void testCreateDeviceBadUUIDAndIPAddress() throws Exception {
        String label = "label";

        DeviceCreateDTO createDTO = DeviceCreateDTO.newBuilder()
            .setLocation("location")
            .setTenantId("BAD")
            .setLabel(label)
            .setManagementIp("BAD")
            .build();

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, ()->serviceStub.createDevice(createDTO));
        Status status = StatusProto.fromThrowable(exception);
        assertThat(status.getCode()).isEqualTo(Code.INVALID_ARGUMENT_VALUE);
        assertThat(status.getMessage()).isEqualTo("Bad tenant_id: BAD, Bad management_ip: BAD");
    }
}
