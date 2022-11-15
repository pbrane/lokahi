package org.opennms.horizon.inventory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opennms.horizon.inventory.dto.IpInterfaceDTO;
import org.opennms.horizon.inventory.dto.MonitoringLocationDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
import org.opennms.horizon.inventory.repository.NodeRepository;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = InventoryApplication.class)
@ContextConfiguration(initializers = {PostgresInitializer.class})
class NodeIT {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private NodeRepository nodeRepository;

    @LocalServerPort
    private Integer port;

    public static long savedMonitoringLocationId = -1;

    @BeforeEach
    public void setup() {
        if (savedMonitoringLocationId == -1) {
            MonitoringLocationDTO monitoringLocationDTO = postMonitoringLocation("location");
            savedMonitoringLocationId = monitoringLocationDTO.getId();
        }
    }

    @AfterEach
    public void teardown() {
        nodeRepository.deleteAll();
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

    @Test
    void testNodeGetAll() throws Exception {
        String nodeLabel = "not here at all";
        postNodes(nodeLabel, new UUID(10, 12));
        postNodes(nodeLabel, new UUID(10, 12));

        ResponseEntity<List> response = this.testRestTemplate
                .getForEntity("http://localhost:" + port + "/inventory/nodes", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(nodeLabel, ((Map)body.get(0)).get("nodeLabel"));
        assertEquals(nodeLabel, ((Map)body.get(1)).get("nodeLabel"));
    }

    @Test
    void testNodePost() throws Exception {
        String nodeLabel = "not here";
        postNodes(nodeLabel, new UUID(10, 12));
    }

    private NodeDTO postNodes(String nodeLabel, UUID tenant) {
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
    void testNodeUpdate() throws Exception {
        String nodeLabel = "not here";
        NodeDTO ml = postNodes(nodeLabel, new UUID(10, 12));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<NodeDTO> request = new HttpEntity<>(ml, headers);

        // Update
        ResponseEntity<NodeDTO> putResponse = this.testRestTemplate
                .exchange("http://localhost:" + port + "/inventory/nodes", HttpMethod.PUT, request, NodeDTO.class);
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());

        // Check there is one database entry
        ResponseEntity<List> response = this.testRestTemplate
                .getForEntity("http://localhost:" + port + "/inventory/nodes", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(1, body.size());
    }

    @Test
    void testNodeGet() throws Exception {
        String nodeLabel = "not here";
        NodeDTO ml = postNodes(nodeLabel, new UUID(10, 12));

        ResponseEntity<NodeDTO> response = this.testRestTemplate
                .getForEntity("http://localhost:" + port + "/inventory/nodes/" + ml.getId(), NodeDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        NodeDTO retrievedML = response.getBody();
        assertEquals(nodeLabel, retrievedML.getNodeLabel());
    }

    @Test
    void testNodeGetNotFound() throws Exception {
        ResponseEntity<NodeDTO> response = this.testRestTemplate
                .getForEntity("http://localhost:" + port + "/inventory/nodes/1", NodeDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testNodeUpdateBadId() throws Exception {
        String nodeLabel = "not here";
        NodeDTO ml = postNodes(nodeLabel, new UUID(10, 12));

        NodeDTO bad = NodeDTO.newBuilder(ml)
                .setId(Long.MAX_VALUE)
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<NodeDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<NodeDTO> response = this.testRestTemplate
                .exchange("http://localhost:" + port + "/inventory/nodes", HttpMethod.PUT, request, NodeDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testNodePostExistingId() throws Exception {
        String nodeLabel = "not here";
        NodeDTO ml = postNodes(nodeLabel, new UUID(10, 12));

        NodeDTO bad = NodeDTO.newBuilder(ml)
                .setNodeLabel("something else")
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<NodeDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<NodeDTO> response = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/inventory/nodes", request, NodeDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testNodeGetByTenantId() throws Exception {
        UUID firstUUID = new UUID(10, 12);
        UUID secondUUID = new UUID(15, 16);
        String firstNodeLabel = "not here at all";
        postNodes(firstNodeLabel, firstUUID);
        String secondNodeLabel = "not there";
        postNodes(secondNodeLabel, firstUUID);
        String thirdNodeLabel = "not there really";
        postNodes(thirdNodeLabel, secondUUID);

        ResponseEntity<List> response = this.testRestTemplate
                .getForEntity("http://localhost:" + port + "/inventory/nodes", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(3, body.size());

        response = this.testRestTemplate
                .getForEntity("http://localhost:" + port + "/inventory/nodes/tenant/" + firstUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(firstNodeLabel, ((Map)body.get(0)).get("nodeLabel"));
        assertEquals(secondNodeLabel, ((Map)body.get(1)).get("nodeLabel"));

        response = this.testRestTemplate
                .getForEntity("http://localhost:" + port + "/inventory/nodes/tenant/" + secondUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(1, body.size());

        assertEquals(thirdNodeLabel, ((Map)body.get(0)).get("nodeLabel"));
    }
}
