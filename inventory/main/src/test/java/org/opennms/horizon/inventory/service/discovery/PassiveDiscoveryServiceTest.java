package org.opennms.horizon.inventory.service.discovery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opennms.horizon.inventory.dto.MonitoredState;
import org.opennms.horizon.inventory.mapper.discovery.PassiveDiscoveryMapper;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.discovery.PassiveDiscovery;
import org.opennms.horizon.inventory.model.node.Node;
import org.opennms.horizon.inventory.repository.discovery.PassiveDiscoveryRepository;
import org.opennms.horizon.inventory.repository.node.DefaultNodeRepository;
import org.opennms.horizon.inventory.service.TagService;
import org.opennms.horizon.inventory.service.taskset.ScannerTaskSetService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassiveDiscoveryServiceTest {

    public static final String TEST_DEFAULT_LOCATION = "Default";
    @Mock
    private PassiveDiscoveryMapper mapper;

    @Mock
    private PassiveDiscoveryRepository repository;

    @Mock
    private TagService tagService;

    @Mock
    private DefaultNodeRepository defaultNodeRepository;

    @Mock
    private ScannerTaskSetService scannerTaskSetService;

    @InjectMocks
    private PassiveDiscoveryService service;

    @Test
    void testSendNodeScan() {
        PassiveDiscovery discovery = new PassiveDiscovery();
        discovery.setLocation(TEST_DEFAULT_LOCATION);
        discovery.setToggle(true);
        discovery.setSnmpCommunities(List.of("public"));
        discovery.setSnmpPorts(List.of(161));

        Optional<PassiveDiscovery> passiveDiscoveryOpt = Optional.of(discovery);

        when(repository.findByTenantIdAndLocation(anyString(), anyString())).thenReturn(passiveDiscoveryOpt);

        MonitoringLocation location = new MonitoringLocation();
        location.setLocation(TEST_DEFAULT_LOCATION);

        Node node = new Node();
        node.setTenantId("tenant-id");
        node.setMonitoringLocation(location);
        node.setMonitoredState(MonitoredState.DETECTED);

        service.sendNodeScan(node);

        verify(scannerTaskSetService).sendNodeScannerTask(any(Node.class), anyString(), anyList());
    }

    @Test
    void testDeleteDiscovery() {
        PassiveDiscovery discovery = new PassiveDiscovery();
        discovery.setId(1L);
        discovery.setLocation(TEST_DEFAULT_LOCATION);
        discovery.setToggle(true);
        discovery.setSnmpCommunities(List.of("public"));
        discovery.setSnmpPorts(List.of(161));

        Optional<PassiveDiscovery> passiveDiscoveryOpt = Optional.of(discovery);
        when(repository.findByTenantIdAndId(anyString(), anyLong())).thenReturn(passiveDiscoveryOpt);

        service.deleteDiscovery("tenant-id", 1L);
        verify(repository, times(1)).delete(eq(discovery));
    }
}
