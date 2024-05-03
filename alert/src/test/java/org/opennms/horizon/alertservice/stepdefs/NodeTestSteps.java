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
package org.opennms.horizon.alertservice.stepdefs;

import static org.junit.Assert.assertTrue;

import com.google.protobuf.InvalidProtocolBufferException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.opennms.horizon.alertservice.RetryUtils;
import org.opennms.horizon.alertservice.kafkahelper.KafkaTestHelper;
import org.opennms.horizon.inventory.dto.IpInterfaceDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;
import org.opennms.horizon.inventory.dto.NodeOperation;
import org.opennms.horizon.inventory.dto.NodeOperationProto;
import org.opennms.horizon.inventory.dto.SnmpInterfaceDTO;

@RequiredArgsConstructor
@Slf4j
public class NodeTestSteps {
    private final KafkaTestHelper kafkaTestHelper;
    private final BackgroundSteps background;
    private final TenantSteps tenantSteps;
    private String nodeTopic;
    private final List<NodeDTO.Builder> builders = new ArrayList<>();
    private final RetryUtils retryUtils;

    @Given("Kafka node topic {string}")
    public void kafkaTagTopic(String nodeTopic) {
        this.nodeTopic = nodeTopic;
        kafkaTestHelper.setKafkaBootstrapUrl(background.getKafkaBootstrapUrl());
        kafkaTestHelper.startConsumerAndProducer(nodeTopic, nodeTopic);
    }

    @Given("[Node] operation data")
    public void nodeData(DataTable data) {
        for (Map<String, String> map : data.asMaps()) {
            NodeDTO.Builder builder = NodeDTO.newBuilder();
            builder.setNodeLabel(map.get("label"))
                    .setId(Long.parseLong(map.get("id")))
                    .setTenantId(map.get("tenant_id"))
                    .addIpInterfaces(IpInterfaceDTO.newBuilder()
                            .setHostname("localhost")
                            .setIpAddress("192.168.1.1")
                            .build())
                    .addSnmpInterfaces(SnmpInterfaceDTO.newBuilder()
                            .setIfAlias("ifAlias")
                            .setNodeId(Long.parseLong(map.get("id")))
                            .setIfIndex(1)
                            .setIfName("eth0")
                            .build());

            builders.add(builder);
        }
    }

    @And("Sent node message to Kafka topic")
    public void sentMessageToKafkaTopic() {
        for (NodeDTO.Builder builder : builders) {
            NodeDTO node = builder.build();
            NodeOperationProto build = NodeOperationProto.newBuilder()
                    .setNodeDto(node)
                    .setOperation(NodeOperation.UPDATE_NODE)
                    .build();
            kafkaTestHelper.sendToTopic(nodeTopic, build.toByteArray(), tenantSteps.getTenantId());
        }
    }

    @Then("Sent node for deletion using Kafka topic")
    public void sentNodeForDeletionUsingKafkaTopic() {
        for (NodeDTO.Builder builder : builders) {
            NodeDTO node = builder.build();
            NodeOperationProto build = NodeOperationProto.newBuilder()
                    .setNodeDto(node)
                    .setOperation(NodeOperation.REMOVE_NODE)
                    .build();
            kafkaTestHelper.sendToTopic(nodeTopic, build.toByteArray(), tenantSteps.getTenantId());
        }
    }

    private boolean checkNumberOfMessageForOneTenant(String tenant, int expectedMessages) {
        int foundMessages = 0;
        List<ConsumerRecord<String, byte[]>> records = kafkaTestHelper.getConsumedMessages(nodeTopic);
        for (ConsumerRecord<String, byte[]> record : records) {
            if (record.value() == null) {
                continue;
            }
            NodeOperationProto nodeOperationProto;
            try {
                nodeOperationProto = NodeOperationProto.parseFrom(record.value());
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

            if (tenant.equals(nodeOperationProto.getNodeDto().getTenantId())) {
                foundMessages++;
            }
        }
        log.info("Found {} messages for tenant {}", foundMessages, tenant);
        return foundMessages == expectedMessages;
    }

    @Then("Verify node topic has {int} message for the tenant id {string}")
    public void verifyNodeTopicHasMessageForTheTenantId(int expectedMessages, String tenant)
            throws InterruptedException {
        boolean success = retryUtils.retry(
                () -> this.checkNumberOfMessageForOneTenant(tenant, expectedMessages),
                result -> result,
                100,
                10000,
                false);

        assertTrue("Verify node topic has the right number of message(s)", success);
    }
}
