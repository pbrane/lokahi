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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.inventory.dto.DefaultNodeDTO;
import org.opennms.horizon.inventory.dto.MonitoredState;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.mapper.node.NodeMapper;
import org.opennms.horizon.inventory.model.IpInterface;
import org.opennms.horizon.inventory.model.MonitoredService;
import org.opennms.horizon.inventory.model.MonitoredServiceType;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.Tag;
import org.opennms.horizon.inventory.model.node.DefaultNode;
import org.opennms.horizon.inventory.model.node.Node;
import org.opennms.horizon.inventory.repository.node.NodeRepository;
import org.opennms.horizon.inventory.service.node.NodeService;
import org.opennms.horizon.inventory.service.taskset.CollectorTaskSetService;
import org.opennms.horizon.inventory.service.taskset.MonitorTaskSetService;
import org.opennms.horizon.inventory.service.taskset.ScannerTaskSetService;
import org.opennms.horizon.inventory.service.taskset.publisher.TaskSetPublisher;
import org.opennms.horizon.shared.utils.InetAddressUtils;
import org.opennms.taskset.contract.MonitorType;
import org.opennms.taskset.contract.TaskDefinition;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NodeServiceTest {

    private NodeService nodeService;
    private NodeRepository mockNodeRepository;
    private NodeMapper mockNodeMapper;
    private final String tenantID = "test-tenant";
    private CollectorTaskSetService mockCollectorTaskSetService;
    private MonitorTaskSetService mockMonitorTaskSetService;

    @BeforeEach
    void prepareTest() {
        mockNodeRepository = mock(NodeRepository.class);
        mockNodeMapper = mock(NodeMapper.class);
        mockCollectorTaskSetService = mock(CollectorTaskSetService.class);
        mockMonitorTaskSetService = mock(MonitorTaskSetService.class);


        nodeService = new NodeService(mockNodeRepository,
            mock(CollectorTaskSetService.class),
            mockMonitorTaskSetService,
            mock(ScannerTaskSetService.class),
            mock(TaskSetPublisher.class),
            mockNodeMapper);
    }

    @Test
    void testFindByTenantId() {
        MonitoringLocation location1 = new MonitoringLocation();
        location1.setLocation("location-1");

        Node node1 = new DefaultNode();
        node1.setId(1L);
        node1.setNodeLabel("node-1");
        node1.setMonitoringLocation(location1);
        node1.setCreateTime(LocalDateTime.now());

        List<Node> nodes = List.of(node1);

        when(mockNodeRepository.findByTenantId(tenantID)).thenReturn(nodes);
        when(mockNodeMapper.modelToDto(nodes)).thenReturn(List.of(NodeDTO.newBuilder()
            .setDefault(DefaultNodeDTO.newBuilder().setId(node1.getId()).build()).build()));

        List<NodeDTO> list = nodeService.findByTenantId(tenantID);
        assertEquals(1, list.size());

        verify(mockNodeRepository, times(1)).findByTenantId(tenantID);
    }

    @Test
    void testGetByIdAndTenantId() {
        MonitoringLocation location1 = new MonitoringLocation();
        location1.setLocation("location-1");

        Node node1 = new DefaultNode();
        node1.setId(1L);
        node1.setNodeLabel("node-1");
        node1.setMonitoringLocation(location1);
        node1.setCreateTime(LocalDateTime.now());

        Optional<Node> node = Optional.of(node1);

        when(mockNodeRepository.findByIdAndTenantId(1L, tenantID)).thenReturn(node);
        when(mockNodeMapper.modelToDto(node.get())).thenReturn(NodeDTO.newBuilder()
            .setDefault(DefaultNodeDTO.newBuilder().setId(node1.getId()).build()).build());

        Optional<NodeDTO> opt = nodeService.getByIdAndTenantId(1L, tenantID);
        assertTrue(opt.isPresent());
        assertTrue(opt.get().hasDefault());

        verify(mockNodeRepository, times(1)).findByIdAndTenantId(1L, tenantID);
    }

    @Test
    void testFindByMonitoredState() {
        MonitoringLocation location1 = new MonitoringLocation();
        location1.setLocation("location-1");

        MonitoringLocation location2 = new MonitoringLocation();
        location2.setLocation("location-2");

        Node node1 = new Node();
        node1.setId(1L);
        node1.setNodeLabel("node-1");
        node1.setMonitoringLocation(location1);
        node1.setCreateTime(LocalDateTime.now());
        node1.setMonitoredState(MonitoredState.DETECTED);
        Node node2 = new Node();
        node2.setId(2L);
        node2.setNodeLabel("node-2");
        node2.setMonitoringLocation(location1);
        node2.setCreateTime(LocalDateTime.now());
        node2.setMonitoredState(MonitoredState.DETECTED);
        Node node3 = new Node();
        node3.setId(3L);
        node3.setNodeLabel("node-3");
        node3.setMonitoringLocation(location2);
        node3.setCreateTime(LocalDateTime.now());
        node3.setMonitoredState(MonitoredState.DETECTED);

        NodeDTO nodeDto1 = NodeDTO.newBuilder().build();
        NodeDTO nodeDto2 = NodeDTO.newBuilder().build();
        NodeDTO nodeDto3 = NodeDTO.newBuilder().build();

        List<Node> nodes = List.of(node1, node2, node3);
        List<NodeDTO> nodeDtoList = List.of(nodeDto1, nodeDto2, nodeDto3);

        when(mockNodeRepository.findByTenantIdAndMonitoredStateEquals(tenantID, MonitoredState.DETECTED)).thenReturn(nodes);
        when(mockNodeMapper.modelToDto(nodes)).thenReturn(nodeDtoList);

        List<NodeDTO> list = nodeService.findByMonitoredState(tenantID, MonitoredState.DETECTED);
        assertEquals(3, list.size());

        verify(mockNodeRepository, times(1)).findByTenantIdAndMonitoredStateEquals(tenantID, MonitoredState.DETECTED);
        verify(mockNodeMapper, times(1)).modelToDto(nodes);
    }

    @Test
    void testDeleteNode() {
        MonitoringLocation location1 = new MonitoringLocation();
        location1.setLocation("location-1");

        Node node1 = new Node();
        node1.setId(1L);
        node1.setNodeLabel("node-1");
        node1.setMonitoringLocation(location1);
        node1.setCreateTime(LocalDateTime.now());
        node1.setMonitoredState(MonitoredState.DETECTED);

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("tag");
        List<Tag> tagList = new ArrayList<>();
        tagList.add(tag);
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(node1);

        node1.setTags(tagList);
        tag.setNodes(nodeList);

        Optional<Node> nodeOpt = Optional.of(node1);
        when(mockNodeRepository.findById(node1.getId())).thenReturn(nodeOpt);

        nodeService.deleteNode(node1.getId());
        verify(mockNodeRepository, times(1)).deleteById(node1.getId());
    }

    @Test
    void testGetTasksForNode() {
        MonitoringLocation location1 = new MonitoringLocation();
        location1.setLocation("location-1");

        MonitoredServiceType monitoredServiceType = new MonitoredServiceType();
        monitoredServiceType.setId(1L);
        monitoredServiceType.setServiceName("ICMP");

        MonitoredService monitoredService = new MonitoredService();
        monitoredService.setId(1L);
        monitoredService.setMonitoredServiceType(monitoredServiceType);

        IpInterface ipInterface = new IpInterface();
        ipInterface.setId(1L);
        ipInterface.setIpAddress(InetAddressUtils.getInetAddress("127.0.0.1"));
        ipInterface.setMonitoredServices(List.of(monitoredService));

        Node node1 = new Node();
        node1.setId(1L);
        node1.setNodeLabel("node-1");
        node1.setMonitoringLocation(location1);
        node1.setCreateTime(LocalDateTime.now());
        node1.setMonitoredState(MonitoredState.DETECTED);
        node1.setIpInterfaces(List.of(ipInterface));

        TaskDefinition taskDefinition = TaskDefinition.newBuilder().build();
        when(mockMonitorTaskSetService.getMonitorTask(any(MonitorType.class), eq(ipInterface), anyLong(), any())).thenReturn(taskDefinition);
        when(mockCollectorTaskSetService.getCollectorTask(any(MonitorType.class), eq(ipInterface), anyLong(), any())).thenReturn(taskDefinition);

        List<TaskDefinition> tasksForNode = nodeService.getTasksForNode(node1);
        assertEquals(1, tasksForNode.size());
    }
}
