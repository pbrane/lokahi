/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.inventory.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.opennms.horizon.inventory.dto.DefaultNodeCreateDTO;
import org.opennms.horizon.inventory.dto.TagCreateDTO;
import org.opennms.horizon.inventory.dto.TagCreateListDTO;
import org.opennms.horizon.inventory.exception.EntityExistException;
import org.opennms.horizon.inventory.mapper.node.DefaultNodeMapper;
import org.opennms.horizon.inventory.model.IpInterface;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.node.DefaultNode;
import org.opennms.horizon.inventory.model.node.Node;
import org.opennms.horizon.inventory.repository.IpInterfaceRepository;
import org.opennms.horizon.inventory.repository.node.DefaultNodeRepository;
import org.opennms.horizon.inventory.service.node.DefaultNodeService;
import org.opennms.horizon.inventory.service.taskset.ScannerTaskSetService;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.opennms.taskset.contract.ScanType;

import java.net.InetAddress;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DefaultNodeServiceTest {
    private DefaultNodeService defaultNodeService;
    private DefaultNodeRepository mockDefaultNodeRepository;
    private IpInterfaceService mockIpInterfaceService;
    private MonitoringLocationService mockMonitoringLocationService;
    private IpInterfaceRepository mockIpInterfaceRepository;
    private TagService tagService;

    @BeforeEach
    void prepareTest() {
        DefaultNodeMapper defaultNodeMapper = Mappers.getMapper(DefaultNodeMapper.class);
        mockDefaultNodeRepository = mock(DefaultNodeRepository.class);
        mockIpInterfaceService = mock(IpInterfaceService.class);
        mockMonitoringLocationService = mock(MonitoringLocationService.class);
        mockIpInterfaceRepository = mock(IpInterfaceRepository.class);
        tagService = mock(TagService.class);


        defaultNodeService = new DefaultNodeService(mockDefaultNodeRepository,
            mockIpInterfaceService,
            mockIpInterfaceRepository,
            mock(ScannerTaskSetService.class),
            mockMonitoringLocationService,
            tagService,
            defaultNodeMapper
        );

        DefaultNode node = new DefaultNode();
        doReturn(node).when(mockDefaultNodeRepository).save(any(node.getClass()));
    }

    @AfterEach
    public void afterTest() {
        verifyNoMoreInteractions(mockDefaultNodeRepository);
        verifyNoMoreInteractions(mockMonitoringLocationService);
        verifyNoMoreInteractions(mockIpInterfaceService);
    }

    @Test
    void createNode() throws Exception {
        String tenant = "ANY";
        String location = "loc";
        MonitoringLocation ml = new MonitoringLocation();
        ml.setTenantId(tenant);
        ml.setLocation(location);

        when(mockMonitoringLocationService.saveMonitoringLocation(eq(tenant), eq(location))).thenReturn(ml);

        DefaultNodeCreateDTO nodeCreateDTO = DefaultNodeCreateDTO.newBuilder()
            .setLabel("Label")
            .setLocation("loc")
            .setManagementIp("127.0.0.1")
            .addTags(TagCreateDTO.newBuilder().setName("tag-name").build())
            .build();

        defaultNodeService.createNode(nodeCreateDTO, ScanType.NODE_SCAN, tenant);
        verify(mockIpInterfaceService).saveIpInterface(eq(tenant), any(Node.class), eq(nodeCreateDTO.getManagementIp()));
        verify(mockDefaultNodeRepository).save(any(DefaultNode.class));
        verify(mockMonitoringLocationService).saveMonitoringLocation(eq(tenant), eq(location));
        verify(tagService).addTags(eq(tenant), any(TagCreateListDTO.class));
    }

    @Test
    void createNodeNoIp() throws Exception {
        String tenant = "TENANT";
        String location = "LOCATION";
        MonitoringLocation ml = new MonitoringLocation();
        ml.setTenantId(tenant);
        ml.setLocation(location);

        when(mockMonitoringLocationService.saveMonitoringLocation(eq(tenant), eq(location))).thenReturn(ml);

        DefaultNodeCreateDTO nodeCreateDTO = DefaultNodeCreateDTO.newBuilder()
            .setLabel("Label")
            .setLocation(location)
            .build();

        defaultNodeService.createNode(nodeCreateDTO, ScanType.NODE_SCAN, tenant);
        verify(mockDefaultNodeRepository).save(any(DefaultNode.class));
        verify(mockMonitoringLocationService).saveMonitoringLocation(eq(tenant), eq(location));
        verifyNoInteractions(mockIpInterfaceService);
    }

    @Test
    void testCreateNodeIPExists() {
        String tenant = "TENANT";

        Node node = new Node();
        IpInterface ipInterface = new IpInterface();
        ipInterface.setNode(node);
        DefaultNodeCreateDTO nodeCreate = DefaultNodeCreateDTO.newBuilder()
            .setLabel("test-node")
            .setManagementIp("127.0.0.1").build();
        doReturn(Optional.of(ipInterface)).when(mockIpInterfaceRepository).findByIpAddressAndLocationAndTenantId(any(InetAddress.class), eq(nodeCreate.getLocation()), eq(tenant));
        assertThatThrownBy(() -> defaultNodeService.createNode(nodeCreate, ScanType.NODE_SCAN, tenant))
            .isInstanceOf(EntityExistException.class)
            .hasMessageContaining("already exists in the system ");
        verify(mockIpInterfaceRepository).findByIpAddressAndLocationAndTenantId(any(InetAddress.class), eq(nodeCreate.getLocation()), eq(tenant));
        verifyNoInteractions(mockDefaultNodeRepository);
        verifyNoInteractions(mockMonitoringLocationService);
        verifyNoInteractions(tagService);
    }
}
