package org.opennms.horizon.inventory.repository;

import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.inventory.SpringContextTestInitializer;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.discovery.active.IcmpActiveDiscovery;
import org.opennms.horizon.inventory.repository.discovery.active.IcmpActiveDiscoveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {SpringContextTestInitializer.class})
@AutoConfigureObservability  // Make sure to include Metrics (for some reason they are disabled by default in the integration grey-box test)
class IcmpActiveDiscoveryRepositoryTest {

    private static final String tenantId = "tenant-1";

    @Autowired
    private IcmpActiveDiscoveryRepository repository;

    @Autowired
    private MonitoringLocationRepository monitoringLocationRepo;

    private Long locationId;

    @BeforeEach
    public void setUp() {
        MonitoringLocation location = new MonitoringLocation();
        location.setTenantId(tenantId);
        location.setLocation("My testing location");
        location = monitoringLocationRepo.save(location);
        locationId = location.getId();
    }

    @Test
    public void testActiveDiscoveryConfigPersistence() {
        var discovery = new IcmpActiveDiscovery();
        discovery.setLocationId(locationId);
        discovery.setName("Profile-1");
        discovery.setTenantId(tenantId);
        discovery.setSnmpPorts(Collections.singletonList(1161));
        discovery.setSnmpCommunityStrings(Collections.singletonList("OpenNMS"));
        discovery.setIpAddressEntries(Arrays.asList("127.0.0.1", "127.0.0.2"));
        discovery.setCreateTime(LocalDateTime.now());
        var persisted = repository.save(discovery);
        Assertions.assertNotNull(persisted);
        Assertions.assertEquals(locationId, discovery.getLocationId());
        Assertions.assertEquals("Profile-1", discovery.getName());
        Assertions.assertEquals("OpenNMS", discovery.getSnmpCommunityStrings().get(0));
        Assertions.assertEquals("127.0.0.1", discovery.getIpAddressEntries().get(0));
        Assertions.assertEquals("127.0.0.2", discovery.getIpAddressEntries().get(1));
        Assertions.assertEquals(1161, discovery.getSnmpPorts().get(0));

        var list = repository.findByLocationIdAndTenantId(locationId, tenantId);
        Assertions.assertFalse(list.isEmpty());

        var optional = repository.findByLocationIdAndName(locationId, "Profile-1");
        Assertions.assertTrue(optional.isPresent());
    }

    @AfterEach
    public void destroy() {
        repository.deleteAll();
    }

}
