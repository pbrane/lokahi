package org.opennms.horizon.inventory.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opennms.horizon.inventory.dto.MonitoredServiceDTO;
import org.opennms.horizon.inventory.mapper.MonitoredServiceMapper;
import org.opennms.horizon.inventory.model.IpInterface;
import org.opennms.horizon.inventory.model.MonitoredService;
import org.opennms.horizon.inventory.model.MonitoredServiceType;
import org.opennms.horizon.inventory.repository.MonitoredServiceRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MonitoredServiceServiceTest {

    @InjectMocks
    private MonitoredServiceService service;

    @Mock
    private MonitoredServiceRepository repository;

    @Mock
    private MonitoredServiceMapper mapper;

    private MonitoredServiceDTO monitoredServiceDto;

    private MonitoredService monitoredService;

    private MonitoredServiceType monitoredServiceType;

    private IpInterface ipInterface;

    @BeforeEach
    public void beforeEach() {
        monitoredServiceDto = MonitoredServiceDTO.newBuilder()
            .setId(1L)
            .setMonitoredServiceTypeId(1L)
            .setTenantId("tenant-id")
            .setIpInterfaceId(1L)
            .build();

        monitoredService = new MonitoredService();
        monitoredService.setId(1L);
        monitoredService.setTenantId("tenant-id");

        monitoredServiceType = new MonitoredServiceType();
        monitoredServiceType.setId(1L);
        monitoredServiceType.setServiceName("ICMP");
        monitoredServiceType.setTenantId("tenant-id");

        ipInterface = new IpInterface();
        ipInterface.setId(1L);
    }

    @Test
    void testCreateSingleAlreadyExist() {
        when(repository.findByTenantIdTypeAndIpInterface(eq("tenant-id"), eq(monitoredServiceType), eq(ipInterface))).thenReturn(Optional.of(monitoredService));
        service.createSingle(monitoredServiceDto, monitoredServiceType, ipInterface);
        verify(repository, times(0)).save(any(MonitoredService.class));
    }

    @Test
    void testCreateSingleDoesNotExist() {
        when(mapper.dtoToModel(any(MonitoredServiceDTO.class))).thenReturn(monitoredService);
        service.createSingle(monitoredServiceDto, monitoredServiceType, ipInterface);
        verify(repository, times(1)).save(any(MonitoredService.class));
    }

    @Test
    void testFindByTenantId() {
        when(repository.findByTenantId("tenant-id")).thenReturn(List.of(monitoredService));
        when(mapper.modelToDTO(any(MonitoredService.class))).thenReturn(monitoredServiceDto);
        List<MonitoredServiceDTO> list = service.findByTenantId("tenant-id");
        Assertions.assertEquals(1, list.size());
        MonitoredServiceDTO dto = list.get(0);
        Assertions.assertEquals(monitoredService.getId(), dto.getId());
    }
}
