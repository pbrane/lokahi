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
package org.opennms.horizon.inventory.component;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.Setter;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.dto.NodeOperation;
import org.opennms.horizon.inventory.dto.NodeOperationProto;
import org.opennms.horizon.inventory.mapper.NodeMapper;
import org.opennms.horizon.inventory.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NodeKafkaProducer {
    @Value("${kafka.topics.node}")
    @Setter // Testability
    private String topic;

    @Autowired
    @Setter // Testability
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    @Setter // Testability
    private NodeMapper nodeMapper;

    @PostUpdate
    @PostPersist
    public void sendNode(Node node) {
        NodeDTO nodeDTO = nodeMapper.modelToDTO(node);
        NodeOperationProto nodeOperationProto = NodeOperationProto.newBuilder()
                .setOperation(NodeOperation.UPDATE_NODE)
                .setNodeDto(nodeDTO)
                .build();

        nodeOperationInKafka(nodeOperationProto);
    }

    public void nodeOperationInKafka(NodeOperationProto nodeOperationProto) {
        var producerRecord = new ProducerRecord<String, byte[]>(topic, nodeOperationProto.toByteArray());
        kafkaTemplate.send(producerRecord);
    }
}
