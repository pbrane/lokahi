package org.opennms.horizon.inventory;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opennms.horizon.inventory.dto.MonitoredServiceTypeDTO;
import org.opennms.horizon.inventory.dto.MonitoringLocationDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.repository.MonitoredServiceTypeRepository;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
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

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = InventoryApplication.class)
@ContextConfiguration(initializers = {PostgresInitializer.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoForeignKeyTableIT {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private MonitoringLocationRepository monitoringLocationRepository;
    @Autowired
    private MonitoredServiceTypeRepository monitoredServiceTypeRepository;
    @Autowired
    private NodeRepository nodeRepository;
    @LocalServerPort
    private Integer port;

    @AfterEach
    public void teardown() {
        monitoringLocationRepository.deleteAll();
        monitoredServiceTypeRepository.deleteAll();
        nodeRepository.deleteAll();
    }

    @Test
    void testMonitoringLocationGetAll() throws Exception {
        String firstLocation = "not here at all";
        postMonitoringLocations(firstLocation, new UUID(10, 12));
        String secondLocation = "not there";
        postMonitoringLocations(secondLocation, new UUID(10, 12));

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/locations", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(firstLocation, ((Map)body.get(0)).get("location"));
        assertEquals(secondLocation, ((Map)body.get(1)).get("location"));
    }

    @Test
    void testMonitoringLocationPost() throws Exception {
        String location = "not here";
        postMonitoringLocations(location, new UUID(10, 12));
    }

    private MonitoringLocationDTO postMonitoringLocations(String location, UUID tenant) {
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
    void testMonitoringLocationViolateUniqueConstraint() throws Exception {
        String location = "not anywhere";
        postMonitoringLocations(location, new UUID(10, 12));

        // location must be unique
        UUID tenant = new UUID(10, 12);
        MonitoringLocationDTO ml = MonitoringLocationDTO.newBuilder()
            .setLocation(location)
            .setTenantId(tenant.toString())
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringLocationDTO> request = new HttpEntity<>(ml, headers);

        ResponseEntity<MonitoringLocationDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/locations", request, MonitoringLocationDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoringLocationUpdate() throws Exception {
        String location = "not here";
        MonitoringLocationDTO ml = postMonitoringLocations(location, new UUID(10, 12));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringLocationDTO> request = new HttpEntity<>(ml, headers);

        // Update
        ResponseEntity<MonitoringLocationDTO> putResponse = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/locations", HttpMethod.PUT, request, MonitoringLocationDTO.class);
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());

        // Check there is one database entry
        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/locations", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(1, body.size());
    }

    @Test
    void testMonitoringLocationGet() throws Exception {
        String location = "not here";
        MonitoringLocationDTO ml = postMonitoringLocations(location, new UUID(10, 12));

        ResponseEntity<MonitoringLocationDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/locations/" + ml.getId(), MonitoringLocationDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        MonitoringLocationDTO retrievedML = response.getBody();
        assertEquals(location, retrievedML.getLocation());
    }

    @Test
    void testMonitoringLocationGetNotFound() throws Exception {
        ResponseEntity<MonitoringLocationDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/locations/1", MonitoringLocationDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testMonitoringLocationUpdateBadId() throws Exception {
        String location = "not here";
        MonitoringLocationDTO ml = postMonitoringLocations(location, new UUID(10, 12));

        MonitoringLocationDTO bad = MonitoringLocationDTO.newBuilder(ml)
                .setId(Long.MAX_VALUE)
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringLocationDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<MonitoringLocationDTO> response = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/locations", HttpMethod.PUT, request, MonitoringLocationDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testMonitoringLocationPostExistingId() throws Exception {
        String location = "not here";
        MonitoringLocationDTO ml = postMonitoringLocations(location, new UUID(10, 12));

        MonitoringLocationDTO bad = MonitoringLocationDTO.newBuilder(ml)
            .setLocation("something else")
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoringLocationDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<MonitoringLocationDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/locations", request, MonitoringLocationDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoringLocationRepoFindByName() {
        String locationName = "testLocation";
        MonitoringLocation location = new MonitoringLocation();
        location.setLocation(locationName);
        location.setTenantId(new UUID(10, 12).toString());
        MonitoringLocation savedLocation = monitoringLocationRepository.saveAndFlush(location);
        assertNotNull(savedLocation);
        Optional<MonitoringLocation> dbLocation = monitoringLocationRepository.findByLocation(locationName);
        assertTrue(dbLocation.isPresent());
        Optional<MonitoringLocation> notExist = monitoringLocationRepository.findByLocation("badname");
        assertFalse(notExist.isPresent());
    }

    @Test
    void testMonitoringLocationGetByTenantId() throws Exception {
        UUID firstUUID = new UUID(10, 12);
        UUID secondUUID = new UUID(15, 16);
        String firstLocation = "not here at all";
        postMonitoringLocations(firstLocation, firstUUID);
        String secondLocation = "not there";
        postMonitoringLocations(secondLocation, firstUUID);
        String thirdLocation = "not there really";
        postMonitoringLocations(thirdLocation, secondUUID);

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/locations", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(3, body.size());

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/locations/tenant/" + firstUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(firstLocation, ((Map)body.get(0)).get("location"));
        assertEquals(secondLocation, ((Map)body.get(1)).get("location"));

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/locations/tenant/" + secondUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(1, body.size());

        assertEquals(thirdLocation, ((Map)body.get(0)).get("location"));
    }

    @Test
    void testMonitoredServiceTypeGetAll() throws Exception {
        String serviceName = "not here at all";
        postMonitoredServiceTypes(serviceName, new UUID(10, 12));
        postMonitoredServiceTypes(serviceName, new UUID(10, 12));

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/serviceTypes", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(serviceName, ((Map)body.get(0)).get("serviceName"));
        assertEquals(serviceName, ((Map)body.get(1)).get("serviceName"));
    }

    @Test
    void testMonitoredServiceTypePost() throws Exception {
        String serviceName = "not here";
        postMonitoredServiceTypes(serviceName, new UUID(10, 12));
    }

    private MonitoredServiceTypeDTO postMonitoredServiceTypes(String serviceName, UUID tenant) {
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
    void testMonitoredServiceTypeUpdate() throws Exception {
        String serviceName = "not here";
        MonitoredServiceTypeDTO ml = postMonitoredServiceTypes(serviceName, new UUID(10, 12));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceTypeDTO> request = new HttpEntity<>(ml, headers);

        // Update
        ResponseEntity<MonitoredServiceTypeDTO> putResponse = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/serviceTypes", HttpMethod.PUT, request, MonitoredServiceTypeDTO.class);
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());

        // Check there is one database entry
        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/serviceTypes", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(1, body.size());
    }

    @Test
    void testMonitoredServiceTypeGet() throws Exception {
        String serviceName = "not here";
        MonitoredServiceTypeDTO ml = postMonitoredServiceTypes(serviceName, new UUID(10, 12));

        ResponseEntity<MonitoredServiceTypeDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/serviceTypes/" + ml.getId(), MonitoredServiceTypeDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        MonitoredServiceTypeDTO retrievedML = response.getBody();
        assertEquals(serviceName, retrievedML.getServiceName());
    }

    @Test
    void testMonitoredServiceTypeGetNotFound() throws Exception {
        ResponseEntity<MonitoredServiceTypeDTO> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/serviceTypes/1", MonitoredServiceTypeDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testMonitoredServiceTypeUpdateBadId() throws Exception {
        String serviceName = "not here";
        MonitoredServiceTypeDTO ml = postMonitoredServiceTypes(serviceName, new UUID(10, 12));

        MonitoredServiceTypeDTO bad = MonitoredServiceTypeDTO.newBuilder(ml)
            .setId(Long.MAX_VALUE)
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceTypeDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<MonitoredServiceTypeDTO> response = this.testRestTemplate
            .exchange("http://localhost:" + port + "/inventory/serviceTypes", HttpMethod.PUT, request, MonitoredServiceTypeDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testMonitoredServiceTypePostExistingId() throws Exception {
        String serviceName = "not here";
        MonitoredServiceTypeDTO ml = postMonitoredServiceTypes(serviceName, new UUID(10, 12));

        MonitoredServiceTypeDTO bad = MonitoredServiceTypeDTO.newBuilder(ml)
            .setServiceName("something else")
            .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MonitoredServiceTypeDTO> request = new HttpEntity<>(bad, headers);

        ResponseEntity<MonitoredServiceTypeDTO> response = this.testRestTemplate
            .postForEntity("http://localhost:" + port + "/inventory/serviceTypes", request, MonitoredServiceTypeDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testMonitoredServiceTypeGetByTenantId() throws Exception {
        UUID firstUUID = new UUID(10, 12);
        UUID secondUUID = new UUID(15, 16);
        String firstServiceName = "not here at all";
        postMonitoredServiceTypes(firstServiceName, firstUUID);
        String secondServiceName = "not there";
        postMonitoredServiceTypes(secondServiceName, firstUUID);
        String thirdServiceName = "not there really";
        postMonitoredServiceTypes(thirdServiceName, secondUUID);

        ResponseEntity<List> response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/serviceTypes", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List body = response.getBody();
        assertEquals(3, body.size());

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/serviceTypes/tenant/" + firstUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(2, body.size());

        assertEquals(firstServiceName, ((Map)body.get(0)).get("serviceName"));
        assertEquals(secondServiceName, ((Map)body.get(1)).get("serviceName"));

        response = this.testRestTemplate
            .getForEntity("http://localhost:" + port + "/inventory/serviceTypes/tenant/" + secondUUID, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        body = response.getBody();
        assertEquals(1, body.size());

        assertEquals(thirdServiceName, ((Map)body.get(0)).get("serviceName"));
    }
}
