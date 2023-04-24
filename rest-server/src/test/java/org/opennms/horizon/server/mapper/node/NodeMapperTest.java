package org.opennms.horizon.server.mapper.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opennms.horizon.inventory.dto.AzureNodeDTO;
import org.opennms.horizon.inventory.dto.DefaultNodeDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.server.model.inventory.node.AzureNode;
import org.opennms.horizon.server.model.inventory.node.DefaultNode;
import org.opennms.horizon.server.model.inventory.node.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class NodeMapperTest {

    @Mock
    private DefaultNodeMapper defaultNodeMapper;

    @Mock
    private AzureNodeMapper azureNodeMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NodeMapper mapper;

    @Test
    void testDtoToNodeDefault() {
        DefaultNodeDTO defaultNodeDTO = DefaultNodeDTO.newBuilder().build();
        NodeDTO nodeDTO = NodeDTO.newBuilder().setDefault(defaultNodeDTO).build();

        DefaultNode defaultNode = new DefaultNode();

        Mockito.when(defaultNodeMapper.protoToNode(defaultNodeDTO)).thenReturn(defaultNode);
        Mockito.when(objectMapper.valueToTree(defaultNode))
            .thenReturn(new ObjectMapper().valueToTree(defaultNode));

        Node node = mapper.dtoToNode(nodeDTO);

        assertEquals("DEFAULT", node.getNodeType());
        assertNotNull(node.getDetails());
    }

    @Test
    void testDtoToNodeAzure() {
        AzureNodeDTO azureNodeDTO = AzureNodeDTO.newBuilder().build();
        NodeDTO nodeDTO = NodeDTO.newBuilder().setAzure(azureNodeDTO).build();

        AzureNode azureNode = new AzureNode();

        Mockito.when(azureNodeMapper.protoToNode(azureNodeDTO)).thenReturn(azureNode);
        Mockito.when(objectMapper.valueToTree(azureNode))
            .thenReturn(new ObjectMapper().valueToTree(azureNode));

        Node node = mapper.dtoToNode(nodeDTO);

        assertEquals("AZURE", node.getNodeType());
        assertNotNull(node.getDetails());
    }

    @Test
    void testDtoToNodeInvalid() {
        NodeDTO nodeDTO = NodeDTO.newBuilder().build();
        assertThrows(RuntimeException.class, () -> {
            mapper.dtoToNode(nodeDTO);
        });
    }
}
