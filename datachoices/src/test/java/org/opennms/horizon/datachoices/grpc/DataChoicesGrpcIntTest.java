package org.opennms.horizon.datachoices.grpc;

import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.VerificationException;
import org.opennms.horizon.datachoices.dto.DataChoicesServiceGrpc;
import org.opennms.horizon.datachoices.dto.ToggleDataChoicesDTO;
import org.opennms.horizon.datachoices.model.DataChoices;
import org.opennms.horizon.datachoices.repository.DataChoicesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DataChoicesGrpcIntTest extends GrpcTestBase {
    private DataChoicesServiceGrpc.DataChoicesServiceBlockingStub serviceStub;

    @Autowired
    private DataChoicesRepository repository;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.5-alpine")
        .withDatabaseName("datachoices").withUsername("datachoices")
        .withPassword("password").withExposedPorts(5432);

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
            () -> String.format("jdbc:postgresql://localhost:%d/%s", postgres.getFirstMappedPort(), postgres.getDatabaseName()));
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void beforeEach() throws VerificationException {
        prepareServer();
        serviceStub = DataChoicesServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    public void cleanUp() throws InterruptedException {
        repository.deleteAll();
        afterTest();
    }

    @Test
    void testToggleOn() {
        ToggleDataChoicesDTO dto = ToggleDataChoicesDTO
            .newBuilder().setToggle(true).build();
        ToggleDataChoicesDTO response = serviceStub
            .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(createAuthHeader(authHeader)))
            .toggle(dto);
        assertEquals(response.getToggle(), dto.getToggle());

        List<DataChoices> all = repository.findAll();
        assertEquals(1, all.size());

        DataChoices dataChoices = all.get(0);
        assertEquals(tenantId, dataChoices.getTenantId());
    }

    @Test
    void testToggleOffWhenOff() {
        ToggleDataChoicesDTO dto = ToggleDataChoicesDTO
            .newBuilder().setToggle(false).build();
        ToggleDataChoicesDTO response = serviceStub
            .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(createAuthHeader(authHeader)))
            .toggle(dto);
        assertEquals(response.getToggle(), dto.getToggle());

        List<DataChoices> all = repository.findAll();
        assertEquals(0, all.size());
    }

    @Test
    void testToggleOnThenOff() {
        ToggleDataChoicesDTO dtoOn = ToggleDataChoicesDTO
            .newBuilder().setToggle(true).build();
        ToggleDataChoicesDTO response = serviceStub
            .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(createAuthHeader(authHeader)))
            .toggle(dtoOn);
        assertEquals(response.getToggle(), dtoOn.getToggle());

        List<DataChoices> all = repository.findAll();
        assertEquals(1, all.size());

        DataChoices dataChoices = all.get(0);
        assertEquals(tenantId, dataChoices.getTenantId());

        ToggleDataChoicesDTO dtoOff = ToggleDataChoicesDTO
            .newBuilder().setToggle(false).build();
        response = serviceStub
            .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(createAuthHeader(authHeader)))
            .toggle(dtoOff);
        assertEquals(response.getToggle(), dtoOff.getToggle());

        all = repository.findAll();
        assertEquals(0, all.size());
    }

}
