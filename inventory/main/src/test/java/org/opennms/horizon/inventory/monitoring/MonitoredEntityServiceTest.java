/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.horizon.inventory.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.Any;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.inventory.mapper.SnmpConfigMapper;
import org.opennms.horizon.inventory.model.DiscoveryMonitoredEntityProvider;
import org.opennms.horizon.inventory.model.IpInterface;
import org.opennms.horizon.inventory.model.MonitoredService;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.repository.MonitoredServiceRepository;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.opennms.horizon.inventory.repository.SnmpConfigRepository;
import org.opennms.horizon.inventory.service.SnmpConfigService;
import org.opennms.horizon.inventory.service.taskset.TaskUtils;
import org.opennms.horizon.inventory.service.taskset.publisher.TaskSetPublisher;
import org.opennms.horizon.shared.utils.InetAddressUtils;
import org.opennms.icmp.contract.IcmpMonitorRequest;

public class MonitoredEntityServiceTest {

    public static final String TEST_TENANT_ID = "x-tenant-id-x";

    private IpInterface testIpInterface;
    private MonitoringLocation location;
    private DiscoveryMonitoredEntityProvider provider;
    private MonitoredEntityService monitoredService;

    private List<MonitoredEntityProvider> listProvider = new ArrayList<>();
    private MonitoringLocationRepository monitoringLocationRepository;
    private TaskSetPublisher taskSetPublisher;

    @BeforeEach
    public void setUp() {
        MonitoredServiceRepository serviceRepository = mock(MonitoredServiceRepository.class);
        SnmpConfigRepository repository = mock(SnmpConfigRepository.class);
        SnmpConfigMapper snmpConfigMapper = mock(SnmpConfigMapper.class);

        location = new MonitoringLocation();
        location.setId(1);
        location.setLocation("default");
        location.setTenantId(TEST_TENANT_ID);

        testIpInterface = new IpInterface();
        testIpInterface.setHostname("x-hostname-x");
        testIpInterface.setLocation(location);
        testIpInterface.setIpAddress(InetAddressUtils.getInetAddress("127.0.0.1"));
        testIpInterface.setSnmpPrimary(true);

        when(serviceRepository.findByTenantIdAndLocationId(TEST_TENANT_ID, location.getId()))
                .thenReturn(populateMonitorService());

        SnmpConfigService snmpConfigService = new SnmpConfigService(repository, snmpConfigMapper);
        provider = new DiscoveryMonitoredEntityProvider(serviceRepository, snmpConfigService);

        monitoringLocationRepository = mock(MonitoringLocationRepository.class);
        taskSetPublisher = mock(TaskSetPublisher.class);

        listProvider.add(provider);

        monitoredService = new MonitoredEntityService(listProvider, monitoringLocationRepository, taskSetPublisher);
    }

    @Test
    void testGetAllMonitoredEntitiesByLocation() {
        List<MonitoredEntity> monitoredEntities = initializeTestDataByLocationAndSearchTerm();

        List<MonitoredEntity> list = monitoredService.getAllMonitoredEntities(TEST_TENANT_ID, location.getId());

        assertEquals(monitoredEntities.size(), list.size());
        assertEquals(monitoredEntities.get(0).getLocationId(), list.get(0).getLocationId());
    }

    @Test
    void testGetAllMonitoredEntities() {
        List<MonitoredEntity> monitoredEntities = initializeTestDataByLocationAndSearchTerm();

        when(monitoringLocationRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(List.of(location));

        List<MonitoredEntity> list = monitoredService.getAllMonitoredEntities(TEST_TENANT_ID);

        assertEquals(monitoredEntities.size(), list.size());
        assertEquals(monitoredEntities.get(0).getLocationId(), list.get(0).getLocationId());
    }

    @Test
    void testGetAllMonitoredEntitiesByLocationWithProvider() {
        List<MonitoredEntity> monitoredEntitiesList = initializeTestDataByLocationAndSearchTerm();

        List<MonitoredEntity> fetchList = provider.getMonitoredEntities(TEST_TENANT_ID, location.getId());

        assertEquals(fetchList.size(), monitoredEntitiesList.size());
        assertEquals(
                fetchList.get(0).getLocationId(), monitoredEntitiesList.get(0).getLocationId());
        assertEquals(fetchList.get(0).getType(), monitoredEntitiesList.get(0).getType());
    }

    @Test
    void testGetAllMonitoredEntitiesBySearchTerm() {
        long locationId = 3L;
        List<MonitoredEntity> monitoredEntitiesList = initializeTestDataByLocationAndSearchTerm();

        List<MonitoredEntity> lst = provider.getMonitoredEntities(TEST_TENANT_ID, locationId);

        List<MonitoredEntity> fetchList = provider.getMonitoredEntities(TEST_TENANT_ID, location.getId());

        assertEquals(fetchList.size(), monitoredEntitiesList.size());
        assertEquals(
                fetchList.get(1).getLocationId(), monitoredEntitiesList.get(1).getLocationId());
        assertEquals(fetchList.get(1).getType(), monitoredEntitiesList.get(1).getType());
        assertNotEquals(lst.size(), monitoredEntitiesList.size());
    }

    @Test
    void testGetAllMonitoredEntitiesByInValidSearchTerm() {
        long locationId = 3L;
        List<MonitoredEntity> lst = provider.getMonitoredEntities(TEST_TENANT_ID, locationId);
        assertTrue(lst.isEmpty());
    }

    @Test
    void testGetAllMonitoredEntitiesWithProvider() {
        List<MonitoredEntity> monitoredEntities = initializeTestDataByLocationAndSearchTerm();
        List<MonitoredEntity> fetchList = provider.getMonitoredEntities(TEST_TENANT_ID, location.getId());
        assertEquals(fetchList.size(), monitoredEntities.size());
    }

    private List<MonitoredEntity> initializeTestDataByLocationAndSearchTerm() {

        MonitoredEntity m1 = new MonitoredEntity.MonitoredEntityBuilder()
                .entityId("1")
                .type("ICMP")
                .locationId(location.getId())
                .config(Any.pack(IcmpMonitorRequest.newBuilder()
                        .setHost(InetAddressUtils.toIpAddrString(testIpInterface.getIpAddress()))
                        .setTimeout(TaskUtils.ICMP_DEFAULT_TIMEOUT_MS)
                        .setDscp(TaskUtils.ICMP_DEFAULT_DSCP)
                        .setAllowFragmentation(TaskUtils.ICMP_DEFAULT_ALLOW_FRAGMENTATION)
                        .setPacketSize(TaskUtils.ICMP_DEFAULT_PACKET_SIZE)
                        .setRetries(TaskUtils.ICMP_DEFAULT_RETRIES)
                        .build()))
                .build();

        MonitoredEntity m2 = new MonitoredEntity.MonitoredEntityBuilder()
                .entityId("2")
                .type("ICMP")
                .locationId(location.getId())
                .config(Any.pack(IcmpMonitorRequest.newBuilder()
                        .setHost(InetAddressUtils.toIpAddrString(testIpInterface.getIpAddress()))
                        .setTimeout(TaskUtils.ICMP_DEFAULT_TIMEOUT_MS)
                        .setDscp(TaskUtils.ICMP_DEFAULT_DSCP)
                        .setAllowFragmentation(TaskUtils.ICMP_DEFAULT_ALLOW_FRAGMENTATION)
                        .setPacketSize(TaskUtils.ICMP_DEFAULT_PACKET_SIZE)
                        .setRetries(TaskUtils.ICMP_DEFAULT_RETRIES)
                        .build()))
                .build();

        MonitoredEntity m3 = new MonitoredEntity.MonitoredEntityBuilder()
                .entityId("3")
                .type("ICMP")
                .locationId(location.getId())
                .config(Any.pack(IcmpMonitorRequest.newBuilder()
                        .setHost(InetAddressUtils.toIpAddrString(testIpInterface.getIpAddress()))
                        .setTimeout(TaskUtils.ICMP_DEFAULT_TIMEOUT_MS)
                        .setDscp(TaskUtils.ICMP_DEFAULT_DSCP)
                        .setAllowFragmentation(TaskUtils.ICMP_DEFAULT_ALLOW_FRAGMENTATION)
                        .setPacketSize(TaskUtils.ICMP_DEFAULT_PACKET_SIZE)
                        .setRetries(TaskUtils.ICMP_DEFAULT_RETRIES)
                        .build()))
                .build();

        return List.of(m1, m2, m3);
    }

    private List<MonitoredService> populateMonitorService() {

        var testMonitoredService1 = new MonitoredService();
        testMonitoredService1.setId(1313);
        testMonitoredService1.setIpInterface(testIpInterface);
        testMonitoredService1.setMonitorType("ICMP");
        testMonitoredService1.setTenantId(TEST_TENANT_ID);

        var testMonitoredService2 = new MonitoredService();
        testMonitoredService2.setId(1717);
        testMonitoredService2.setIpInterface(testIpInterface);
        testMonitoredService2.setMonitorType("ICMP");
        testMonitoredService2.setTenantId(TEST_TENANT_ID);

        var testMonitoredService3 = new MonitoredService();
        testMonitoredService3.setId(1919);
        testMonitoredService3.setIpInterface(testIpInterface);
        testMonitoredService3.setMonitorType("ICMP");

        return List.of(testMonitoredService1, testMonitoredService2, testMonitoredService3);
    }
}
