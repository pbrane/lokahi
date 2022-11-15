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
import org.opennms.horizon.inventory.dto.MonitoringLocationDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
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
class IpInterfaceIT {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private IpInterfaceRepository ipInterfaceRepository;
    @LocalServerPort
    private Integer port;
    public static long savedNodeId = -1;
    public static long savedMonitoringLocationId = -1;

    @BeforeEach
    public void setup() {
        if (savedMonitoringLocationId == -1) {
            MonitoringLocationDTO monitoringLocationDTO = postMonitoringLocation("location");
            savedMonitoringLocationId = monitoringLocationDTO.getId();

            NodeDTO dto = postNode("label");
            savedNodeId = dto.getId();
        }
    }

    @AfterEach
    public void teardown() {
        ipInterfaceRepository.deleteAll();
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

    @Test
    void testGetAll() throws Exception {
        String ipAddress = "127.0.0.2";
        postIpInterface(ipAddress);
        postIpInterface(ipAddress);

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/ipInterfaces", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(ipAddress, ((Map)body.get(0)).get("ipAddress"));
        assertEquals(ipAddress, ((Map)body.get(1)).get("ipAddress"));
    }

    @Test
    void testPost() throws Exception {
        String ipAddress = "127.0.0.1";
        postIpInterface(ipAddress);
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

    @Test
    void testUpdate() throws Exception {
        String ipAddress = "127.0.0.1";
        IpInterfaceDTO ml = postIpInterface(ipAddress);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<IpInterfaceDTO> request = new HttpEntity<>(ml, headers);

        // Update
        ResponseEntity<IpInterfaceDTO> putResponse = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/ipInterfaces", HttpMethod.PUT, request, IpInterfaceDTO.class);
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());

        // Check there is one database entry
        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/ipInterfaces", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(1, body.size());
    }

    @Test
    void testGet() throws Exception {
        String ipAddress = "127.0.0.1";
        IpInterfaceDTO ml = postIpInterface(ipAddress);

        ResponseEntity<IpInterfaceDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/ipInterfaces/" + ml.getId(), IpInterfaceDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        IpInterfaceDTO retrievedML = response.getBody();
        assertEquals(ipAddress, retrievedML.getIpAddress());
    }

    @Test
    void testGetNotFound() throws Exception {
        ResponseEntity<IpInterfaceDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/ipInterfaces/1", IpInterfaceDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateBadId() throws Exception {
        String ipAddress = "127.0.0.1";
        IpInterfaceDTO ml = postIpInterface(ipAddress);

        IpInterfaceDTO bad = IpInterfaceDTO.newBuilder(ml)
                .setId(Long.MAX_VALUE)
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<IpInterfaceDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<IpInterfaceDTO> response = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/ipInterfaces", HttpMethod.PUT, request, IpInterfaceDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testPostExistingId() throws Exception {
        String ipAddress = "127.0.0.1";
        IpInterfaceDTO ml = postIpInterface(ipAddress);

        IpInterfaceDTO bad = IpInterfaceDTO.newBuilder(ml)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<IpInterfaceDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<IpInterfaceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/ipInterfaces", request, IpInterfaceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testPostBadNodeId() throws Exception {
        String ipAddress = "127.0.0.1";
        UUID tenant = new UUID(10, 12);

        IpInterfaceDTO ml = IpInterfaceDTO.newBuilder()
            .setIpAddress(ipAddress)
            .setTenantId(tenant.toString())
            .setNodeId(Long.MAX_VALUE)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<IpInterfaceDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<IpInterfaceDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/ipInterfaces", request, IpInterfaceDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testPostBadIPAddress() throws Exception {
        String ipAddress = "bad";
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

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
