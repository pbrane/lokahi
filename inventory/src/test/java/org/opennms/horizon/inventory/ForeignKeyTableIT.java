package org.opennms.horizon.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opennms.horizon.inventory.dto.IpInterfaceDTO;
import org.opennms.horizon.inventory.dto.MonitoredServiceDTO;
import org.opennms.horizon.inventory.dto.MonitoredServiceTypeDTO;
import org.opennms.horizon.inventory.dto.MonitoringLocationDTO;
import org.opennms.horizon.inventory.dto.MonitoringSystemDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.dto.SnmpInterfaceDTO;
import org.opennms.horizon.inventory.repository.MonitoredServiceRepository;
import org.opennms.horizon.inventory.repository.MonitoringSystemRepository;
import org.opennms.horizon.inventory.repository.SnmpInterfaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = InventoryApplication.class)
@ContextConfiguration(initializers = {PostgresInitializer.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ForeignKeyTableIT {
    public static final String SYS_ID = "SYS_ID";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private MonitoredServiceRepository monitoredServiceRepository;

    @Autowired
    private SnmpInterfaceRepository snmpInterfaceRepository;

    @Autowired
    private MonitoringSystemRepository monitoringSystemRepository;

    @LocalServerPort
    private Integer port;

    public static long savedNodeId = -1;
    public static long savedIpInterfaceId = -1;
    public static long savedMonitorServiceTypeId = -1;
    public static long savedMonitoringLocationId = -1;

    @BeforeEach
    public void setup() {
        assertTrue(postgres.isCreated());
        assertTrue(postgres.isRunning());

        if (savedMonitoringLocationId == -1) {
            MonitoringLocationDTO monitoringLocationDTO = postMonitoringLocation("location");
            savedMonitoringLocationId = monitoringLocationDTO.getId();

            NodeDTO nodeDTO = postNode("label");
            savedNodeId = nodeDTO.getId();

            IpInterfaceDTO ipInterfaceDTO = postIpInterface("127.0.0.1");
            savedIpInterfaceId = ipInterfaceDTO.getId();

            MonitoredServiceTypeDTO monitoredServiceTypeDTO = postMonitoredServiceTypes("serviceName");
            savedMonitorServiceTypeId = monitoredServiceTypeDTO.getId();
        }
    }

    @AfterEach
    public void teardown() {
        monitoredServiceRepository.deleteAll();
        snmpInterfaceRepository.deleteAll();
        monitoringSystemRepository.deleteAll();
    }

    private MonitoringLocationDTO postMonitoringLocation(String location) {
        UUID tenant = new UUID(10, 12);
        MonitoringLocationDTO ml = MonitoringLocationDTO.newBuilder()
            .setLocation(location)
            .setTenantId(tenant.toString())
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringLocationDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoringLocationDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/locations", request, MonitoringLocationDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        MonitoringLocationDTO saved = response.getBody();
        assertEquals(tenant.toString(), saved.getTenantId());
        assertEquals(location, saved.getLocation());
        return saved;
    }

    private NodeDTO postNode(String nodeLabel) {
        UUID tenant = new UUID(10, 12);
        NodeDTO ml = NodeDTO.newBuilder()
            .setNodeLabel(nodeLabel)
            .setTenantId(tenant.toString())
            .setCreateTime("2022-11-03T14:34:05.542488")
            .setMonitoringLocationId(savedMonitoringLocationId)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<NodeDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<NodeDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/nodes", request, NodeDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        NodeDTO saved = response.getBody();
        assertEquals(tenant.toString(), saved.getTenantId());
        assertEquals(nodeLabel, saved.getNodeLabel());
        return saved;
    }

    private IpInterfaceDTO postIpInterface(String ipAddress) {
        UUID tenant = new UUID(10, 12);

        IpInterfaceDTO ml = IpInterfaceDTO.newBuilder()
                .setIpAddress(ipAddress)
                .setTenantId(tenant.toString())
                .setNodeId(savedNodeId)
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<IpInterfaceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<IpInterfaceDTO> response = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/inventory/ipInterfaces", request, IpInterfaceDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        IpInterfaceDTO saved = response.getBody();
        assertEquals(tenant.toString(), saved.getTenantId());
        assertEquals(ipAddress, saved.getIpAddress());
        return saved;
    }

    private MonitoredServiceTypeDTO postMonitoredServiceTypes(String serviceName) {
        UUID tenant = new UUID(10, 12);
        MonitoredServiceTypeDTO ml = MonitoredServiceTypeDTO.newBuilder()
                .setServiceName(serviceName)
                .setTenantId(tenant.toString())
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceTypeDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoredServiceTypeDTO> response = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/inventory/serviceTypes", request, MonitoredServiceTypeDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        MonitoredServiceTypeDTO saved = response.getBody();
        assertEquals(tenant.toString(), saved.getTenantId());
        assertEquals(serviceName, saved.getServiceName());
        return saved;
    }

    @Test
    void testMonitoredServiceGetAll() throws Exception {
        postMonitoredService(new UUID(10, 12));
        postMonitoredService(new UUID(10, 12));

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/services", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(String.valueOf(savedIpInterfaceId), ((Map)body.get(0)).get("ipInterfaceId"));
        assertEquals(String.valueOf(savedIpInterfaceId), ((Map)body.get(1)).get("ipInterfaceId"));
    }

    @Test
    void testMonitoredServicePost() throws Exception {
        postMonitoredService(new UUID(10, 12));
    }

    private MonitoredServiceDTO postMonitoredService(UUID tenant) {
        MonitoredServiceDTO ml = MonitoredServiceDTO.newBuilder()
            .setIpInterfaceId(savedIpInterfaceId)
            .setTenantId(tenant.toString())
            .setMonitoredServiceTypeId(savedMonitorServiceTypeId)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoredServiceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/services", request, MonitoredServiceDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        MonitoredServiceDTO saved = response.getBody();
        assertEquals(tenant.toString(), saved.getTenantId());
        assertEquals(savedMonitorServiceTypeId, saved.getMonitoredServiceTypeId());
        return saved;
    }

    @Test
    void testMonitoredServiceUpdate() throws Exception {
        MonitoredServiceDTO ml = postMonitoredService(new UUID(10, 12));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceDTO> request = new HttpEntity<>(ml, headers);

        // Update
        ResponseEntity<MonitoredServiceDTO> putResponse = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/services", HttpMethod.PUT, request, MonitoredServiceDTO.class);
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());

        // Check there is one database entry
        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/services", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(1, body.size());
    }

    @Test
    void testMonitoredServiceGet() throws Exception {
        MonitoredServiceDTO ml = postMonitoredService(new UUID(10, 12));

        ResponseEntity<MonitoredServiceDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/services/" + ml.getId(), MonitoredServiceDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        MonitoredServiceDTO retrievedML = response.getBody();
        assertEquals(savedIpInterfaceId, retrievedML.getIpInterfaceId());
        assertEquals(savedMonitorServiceTypeId, retrievedML.getMonitoredServiceTypeId());
    }

    @Test
    void testMonitoredServiceGetNotFound() throws Exception {
        ResponseEntity<MonitoredServiceDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/services/1", MonitoredServiceDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testMonitoredServiceUpdateBadId() throws Exception {
        MonitoredServiceDTO ml = postMonitoredService(new UUID(10, 12));

        MonitoredServiceDTO bad = MonitoredServiceDTO.newBuilder(ml)
                .setId(Long.MAX_VALUE)
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<MonitoredServiceDTO> response = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/services", HttpMethod.PUT, request, MonitoredServiceDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testMonitoredServicePostExistingId() throws Exception {
        MonitoredServiceDTO ml = postMonitoredService(new UUID(10, 12));

        MonitoredServiceDTO bad = MonitoredServiceDTO.newBuilder(ml)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<MonitoredServiceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/services", request, MonitoredServiceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoredServicePostBadIpInterfaceId() throws Exception {
        UUID tenant = new UUID(10, 12);

        MonitoredServiceDTO ml = MonitoredServiceDTO.newBuilder()
                .setIpInterfaceId(savedIpInterfaceId)
                .setTenantId(tenant.toString())
                .setMonitoredServiceTypeId(Long.MAX_VALUE)
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoredServiceDTO> response = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/inventory/services", request, MonitoredServiceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoredServicePostBadMonitoredServiceTypeId() throws Exception {
        UUID tenant = new UUID(10, 12);

        MonitoredServiceDTO ml = MonitoredServiceDTO.newBuilder()
                .setIpInterfaceId(Long.MAX_VALUE)
                .setTenantId(tenant.toString())
                .setMonitoredServiceTypeId(savedMonitorServiceTypeId)
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoredServiceDTO> response = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/inventory/services", request, MonitoredServiceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoredServicePostBadTenantId() throws Exception {
        MonitoredServiceDTO ml = MonitoredServiceDTO.newBuilder()
            .setIpInterfaceId(savedIpInterfaceId)
            .setTenantId("0000")
            .setMonitoredServiceTypeId(savedMonitorServiceTypeId)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoredServiceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/services", request, MonitoredServiceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoredServiceGetByTenantId() throws Exception {
        UUID firstUUID = new UUID(10, 12);
        UUID secondUUID = new UUID(15, 16);
        postMonitoredService(firstUUID);
        postMonitoredService(firstUUID);
        postMonitoredService(secondUUID);

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/services", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(3, body.size());

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/services/tenant/" + firstUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(firstUUID.toString(), ((Map)body.get(0)).get("tenantId"));

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/services/tenant/" + secondUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(1, body.size());

        assertEquals(secondUUID.toString(), ((Map)body.get(0)).get("tenantId"));
    }

    @Test
    void testSnmpInterfaceGetAll() throws Exception {
        String ipAddress = "127.0.0.2";
        postSnmpInterface(ipAddress, new UUID(10, 12));
        postSnmpInterface(ipAddress, new UUID(10, 12));

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/snmpInterfaces", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(ipAddress, ((Map)body.get(0)).get("ipAddress"));
        assertEquals(ipAddress, ((Map)body.get(1)).get("ipAddress"));
    }

    @Test
    void testSnmpInterfacePost() throws Exception {
        String ipAddress = "127.0.0.1";
        postSnmpInterface(ipAddress, new UUID(10, 12));
    }

    private SnmpInterfaceDTO postSnmpInterface(String ipAddress, UUID tenant) {
        SnmpInterfaceDTO ml = SnmpInterfaceDTO.newBuilder()
            .setIpAddress(ipAddress)
            .setTenantId(tenant.toString())
            .setNodeId(savedNodeId)
            .setIfIndex(4)
            .setIfDescr("Desc")
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SnmpInterfaceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<SnmpInterfaceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/snmpInterfaces", request, SnmpInterfaceDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        SnmpInterfaceDTO saved = response.getBody();
        assertEquals(tenant.toString(), saved.getTenantId());
        assertEquals(ipAddress, saved.getIpAddress());
        return saved;
    }

    @Test
    void testSnmpInterfaceUpdate() throws Exception {
        String ipAddress = "127.0.0.1";
        SnmpInterfaceDTO ml = postSnmpInterface(ipAddress, new UUID(10, 12));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SnmpInterfaceDTO> request = new HttpEntity<>(ml, headers);

        // Update
        ResponseEntity<SnmpInterfaceDTO> putResponse = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/snmpInterfaces", HttpMethod.PUT, request, SnmpInterfaceDTO.class);
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());

        // Check there is one database entry
        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/snmpInterfaces", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(1, body.size());
    }

    @Test
    void testSnmpInterfaceGet() throws Exception {
        String ipAddress = "127.0.0.1";
        SnmpInterfaceDTO ml = postSnmpInterface(ipAddress, new UUID(10, 12));

        ResponseEntity<SnmpInterfaceDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/snmpInterfaces/" + ml.getId(), SnmpInterfaceDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        SnmpInterfaceDTO retrievedML = response.getBody();
        assertEquals(ipAddress, retrievedML.getIpAddress());
    }

    @Test
    void testSnmpInterfaceGetNotFound() throws Exception {
        ResponseEntity<SnmpInterfaceDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/snmpInterfaces/1", SnmpInterfaceDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testSnmpInterfaceUpdateBadId() throws Exception {
        String ipAddress = "127.0.0.1";
        SnmpInterfaceDTO ml = postSnmpInterface(ipAddress, new UUID(10, 12));

        SnmpInterfaceDTO bad = SnmpInterfaceDTO.newBuilder(ml)
            .setId(Long.MAX_VALUE)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SnmpInterfaceDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<SnmpInterfaceDTO> response = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/snmpInterfaces", HttpMethod.PUT, request, SnmpInterfaceDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testSnmpInterfacePostExistingId() throws Exception {
        String ipAddress = "127.0.0.1";
        SnmpInterfaceDTO ml = postSnmpInterface(ipAddress, new UUID(10, 12));

        SnmpInterfaceDTO bad = SnmpInterfaceDTO.newBuilder(ml)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SnmpInterfaceDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<SnmpInterfaceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/snmpInterfaces", request, SnmpInterfaceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSnmpInterfacePostBadNodeId() throws Exception {
        String ipAddress = "127.0.0.1";
        UUID tenant = new UUID(10, 12);

        SnmpInterfaceDTO ml = SnmpInterfaceDTO.newBuilder()
            .setIpAddress(ipAddress)
            .setTenantId(tenant.toString())
            .setNodeId(Long.MAX_VALUE)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SnmpInterfaceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<SnmpInterfaceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/snmpInterfaces", request, SnmpInterfaceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSnmpInterfacePostBadTenantId() throws Exception {
        String ipAddress = "127.0.0.1";

        SnmpInterfaceDTO ml = SnmpInterfaceDTO.newBuilder()
            .setIpAddress(ipAddress)
            .setTenantId("0000")
            .setNodeId(savedNodeId)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SnmpInterfaceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<SnmpInterfaceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/snmpInterfaces", request, SnmpInterfaceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSnmpInterfacePostBadIPAddress() throws Exception {
        String ipAddress = "bad";
        UUID tenant = new UUID(10, 12);

        SnmpInterfaceDTO ml = SnmpInterfaceDTO.newBuilder()
            .setIpAddress(ipAddress)
            .setTenantId(tenant.toString())
            .setNodeId(savedNodeId)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SnmpInterfaceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<SnmpInterfaceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/snmpInterfaces", request, SnmpInterfaceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSnmpInterfacePostWithNullFields() {
        UUID tenant = new UUID(10, 12);

        SnmpInterfaceDTO ml = SnmpInterfaceDTO.newBuilder()
            .setTenantId(tenant.toString())
            .setNodeId(savedNodeId)
            .setIfIndex(4)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SnmpInterfaceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<SnmpInterfaceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/snmpInterfaces", request, SnmpInterfaceDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        SnmpInterfaceDTO saved = response.getBody();
        assertEquals(tenant.toString(), saved.getTenantId());
        assertEquals("", saved.getIpAddress());
        assertEquals("", saved.getIfDescr());
    }

    @Test
    void testSnmpInterfaceGetByTenantId() throws Exception {
        UUID firstUUID = new UUID(10, 12);
        UUID secondUUID = new UUID(15, 16);
        String first = "127.0.0.1";
        postSnmpInterface(first, firstUUID);
        String second = "127.0.0.2";
        postSnmpInterface(second, firstUUID);
        String third = "127.0.0.3";
        postSnmpInterface(third, secondUUID);

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/snmpInterfaces", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(3, body.size());

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/snmpInterfaces/tenant/" + firstUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(first, ((Map)body.get(0)).get("ipAddress"));
        assertEquals(second, ((Map)body.get(1)).get("ipAddress"));

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/snmpInterfaces/tenant/" + secondUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(1, body.size());

        assertEquals(third, ((Map)body.get(0)).get("ipAddress"));
    }

    @Test
    void testMonitoringSystemGetAll() throws Exception {
        postMonitoringSystem(new UUID(10, 12));
        postMonitoringSystem(new UUID(10, 12));

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/monitoringSystems", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(SYS_ID, ((Map)body.get(0)).get("systemId"));
        assertEquals(SYS_ID, ((Map)body.get(1)).get("systemId"));
    }

    @Test
    void testMonitoringSystemPost() throws Exception {
        postMonitoringSystem(new UUID(10, 12));
    }

    private MonitoringSystemDTO postMonitoringSystem(UUID tenant) {
        MonitoringSystemDTO ml = MonitoringSystemDTO.newBuilder()
            .setTenantId(tenant.toString())
            .setSystemId(SYS_ID)
            .setLabel("LABEL")
            .setMonitoringLocationId(savedMonitoringLocationId)
            .setLastCheckedIn(System.currentTimeMillis())
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringSystemDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoringSystemDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/monitoringSystems", request, MonitoringSystemDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        MonitoringSystemDTO saved = response.getBody();
        assertEquals(tenant.toString(), saved.getTenantId());
        assertEquals(SYS_ID, saved.getSystemId());
        return saved;
    }

    @Test
    void testMonitoringSystemUpdate() throws Exception {
        MonitoringSystemDTO ml = postMonitoringSystem(new UUID(10, 12));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringSystemDTO> request = new HttpEntity<>(ml, headers);

        // Update
        ResponseEntity<MonitoringSystemDTO> putResponse = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/monitoringSystems", HttpMethod.PUT, request, MonitoringSystemDTO.class);
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());

        // Check there is one database entry
        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/monitoringSystems", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(1, body.size());
    }

    @Test
    void testMonitoringSystemGet() throws Exception {
        MonitoringSystemDTO ml = postMonitoringSystem(new UUID(10, 12));

        ResponseEntity<MonitoringSystemDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/monitoringSystems/" + ml.getId(), MonitoringSystemDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        MonitoringSystemDTO retrievedML = response.getBody();
        assertEquals(SYS_ID, retrievedML.getSystemId());
    }

    @Test
    void testMonitoringSystemGetNotFound() throws Exception {
        ResponseEntity<MonitoringSystemDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/monitoringSystems/1", MonitoringSystemDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testMonitoringSystemUpdateBadId() throws Exception {
        MonitoringSystemDTO ml = postMonitoringSystem(new UUID(10, 12));

        MonitoringSystemDTO bad = MonitoringSystemDTO.newBuilder(ml)
            .setId(Long.MAX_VALUE)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringSystemDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<MonitoringSystemDTO> response = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/monitoringSystems", HttpMethod.PUT, request, MonitoringSystemDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testMonitoringSystemPostExistingId() throws Exception {
        MonitoringSystemDTO ml = postMonitoringSystem(new UUID(10, 12));

        MonitoringSystemDTO bad = MonitoringSystemDTO.newBuilder(ml)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringSystemDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<MonitoringSystemDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/monitoringSystems", request, MonitoringSystemDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoringSystemPostBadMonitoringLocationId() throws Exception {
        UUID tenant = new UUID(10, 12);

        MonitoringSystemDTO ml = MonitoringSystemDTO.newBuilder()
            .setTenantId(tenant.toString())
            .setSystemId(SYS_ID)
            .setLabel("LABEL")
            .setMonitoringLocationId(Long.MAX_VALUE)
            .setLastCheckedIn(System.currentTimeMillis())
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringSystemDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoringSystemDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/monitoringSystems", request, MonitoringSystemDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoringSystemPostBadTenantId() throws Exception {
        MonitoringSystemDTO ml = MonitoringSystemDTO.newBuilder()
            .setTenantId("0000")
            .setSystemId(SYS_ID)
            .setLabel("LABEL")
            .setMonitoringLocationId(savedMonitoringLocationId)
            .setLastCheckedIn(System.currentTimeMillis())
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringSystemDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoringSystemDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/monitoringSystems", request, MonitoringSystemDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoringSystemGetByTenantId() throws Exception {
        UUID firstUUID = new UUID(10, 12);
        UUID secondUUID = new UUID(15, 16);
        postMonitoringSystem(firstUUID);
        postMonitoringSystem(firstUUID);
        postMonitoringSystem(secondUUID);

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/monitoringSystems", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(3, body.size());

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/monitoringSystems/tenant/" + firstUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(firstUUID.toString(), ((Map)body.get(0)).get("tenantId"));

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/monitoringSystems/tenant/" + secondUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(1, body.size());

        assertEquals(secondUUID.toString(), ((Map)body.get(0)).get("tenantId"));
    }
}
