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
package org.opennms.horizon.events.stepdefs;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.opennms.horizon.events.EventsBackgroundHelper;
import org.opennms.horizon.events.kafkahelper.KafkaTestHelper;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventLog;
import org.opennms.horizon.events.util.RetryUtils;
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
    private String tenantId;
    private String nodeTopic;
    private final List<NodeDTO.Builder> builders = new ArrayList<>();
    private final RetryUtils retryUtils;
    private final EventsBackgroundHelper backgroundHelper;

    @Given("Kafka node topic {string}")
    public void kafkaTagTopic(String nodeTopic) {
        this.nodeTopic = nodeTopic;
        kafkaTestHelper.setKafkaBootstrapUrl(background.getKafkaBootstrapUrl());
        kafkaTestHelper.startConsumerAndProducer(nodeTopic, nodeTopic);
    }

    @Given("Tenant id {string}")
    @Given("Tenant {string}")
    @Given("A new tenant named {string}")
    public String useSpecifiedTenantId(String tenantId) {
        this.tenantId = tenantId;
        log.info("New tenant-id is {}", tenantId);
        return tenantId;
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
            kafkaTestHelper.sendToTopic(nodeTopic, build.toByteArray(), tenantId);
        }
    }

    @When("An event is sent with UEI {string} on node {int}")
    public void sendEvent(String uei, int nodeId) {
        sendEvent(uei, nodeId, System.currentTimeMillis());
    }

    @Then("Sent node for deletion using Kafka topic")
    public void sentNodeForDeletionUsingKafkaTopic() {
        for (NodeDTO.Builder builder : builders) {
            NodeDTO node = builder.build();
            NodeOperationProto build = NodeOperationProto.newBuilder()
                    .setNodeDto(node)
                    .setOperation(NodeOperation.REMOVE_NODE)
                    .build();
            kafkaTestHelper.sendToTopic(nodeTopic, build.toByteArray(), tenantId);
        }
    }

    private boolean checkNumberOfMessageForOneTenant(int expectedMessages, NodeOperation operation) {
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

            if (operation.equals(nodeOperationProto.getOperation())) {
                foundMessages++;
            }
        }
        log.info("Found {} messages for tenant {}", foundMessages, operation);
        return foundMessages == expectedMessages;
    }

    public void sendEvent(String uei, int nodeId, long producedTimeMs) {
        EventLog eventLog = EventLog.newBuilder()
                .setTenantId(tenantId)
                .addEvents(Event.newBuilder()
                        .setTenantId(tenantId)
                        .setProducedTimeMs(producedTimeMs)
                        .setNodeId(nodeId)
                        .setUei(uei))
                .build();

        kafkaTestHelper.sendToTopic(background.getEventTopic(), eventLog.toByteArray(), tenantId);
    }

    @Then("Verify node topic has {int} message for node operation {string}")
    public void verifyNodeTopicHasMessageForNodeOperation(int expectedMessages, String nodeOperation)
            throws InterruptedException {
        boolean success = retryUtils.retry(
                () -> this.checkNumberOfMessageForOneTenant(expectedMessages, NodeOperation.valueOf(nodeOperation)),
                result -> result,
                100,
                10000,
                false);

        assertTrue("Verify node topic has the right number of message(s)", success);
    }

    @Then("Verify all events are remove once node with id {int} is deleted")
    public void verifyAllEventsAreRemoveOnceNodeWithIdIsDeleted(long nodeId) {

        await().atMost(20, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    EventLog eventLog = backgroundHelper
                            .getEventServiceBlockingStub()
                            .listEvents(Empty.newBuilder().build());
                    List<Event> eventFilterList = eventLog.getEventsList().stream()
                            .filter(x -> x.getNodeId() == nodeId)
                            .toList();
                    assertEquals(eventFilterList.size(), 0);
                });
    }
}
