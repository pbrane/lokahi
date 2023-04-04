package org.opennms.horizon.inventory.mapper.node;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opennms.horizon.inventory.dto.AzureNodeDTO;
import org.opennms.horizon.inventory.dto.DefaultNodeDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.model.node.AzureNode;
import org.opennms.horizon.inventory.model.node.DefaultNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeMapperTest {
    @InjectMocks
    private NodeMapper mapper;

    @Mock
    private DefaultNodeMapper defaultNodeMapper;

    @Mock
    private AzureNodeMapper azureNodeMapper;

    private DefaultNode defaultNode;
    private AzureNode azureNode;

    @BeforeEach
    public void setup() {
        defaultNode = new DefaultNode();
        defaultNode.setId(1L);

        DefaultNodeDTO defaultNodeDTO = DefaultNodeDTO.newBuilder().setId(defaultNode.getId()).build();
        when(defaultNodeMapper.modelToDto(defaultNode)).thenReturn(defaultNodeDTO);

        azureNode = new AzureNode();
        azureNode.setId(2L);

        AzureNodeDTO azureNodeDTO = AzureNodeDTO.newBuilder().setId(azureNode.getId()).build();
        when(azureNodeMapper.modelToDto(azureNode)).thenReturn(azureNodeDTO);
    }

    @Test
    void testModelToDto() {
        List<NodeDTO> dtos = mapper.modelToDto(List.of(defaultNode, azureNode));
        assertEquals(2, dtos.size());

        NodeDTO node1 = dtos.get(0);
        assertTrue(node1.hasDefault());
        assertEquals(defaultNode.getId(), node1.getDefault().getId());

        NodeDTO node2 = dtos.get(1);
        assertTrue(node2.hasAzure());
        assertEquals(azureNode.getId(), node2.getAzure().getId());
    }
}
