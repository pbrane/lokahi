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
import static org.junit.jupiter.api.Assertions.*;

import com.google.protobuf.Empty;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hamcrest.Matchers;
import org.opennms.horizon.events.EventsBackgroundHelper;
import org.opennms.horizon.events.kafkahelper.KafkaTestHelper;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventLog;
import org.opennms.horizon.events.proto.EventsSearchBy;
import org.opennms.horizon.grpc.traps.contract.TenantLocationSpecificTrapLogDTO;
import org.opennms.horizon.grpc.traps.contract.TrapDTO;
import org.opennms.horizon.metrics.threshold.proto.ThresholdAlert;
import org.opennms.horizon.metrics.threshold.proto.ThresholdAlertData;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class EventStepDefinitions {
    private final EventsBackgroundHelper backgroundHelper;
    private static final Logger LOG = LoggerFactory.getLogger(EventStepDefinitions.class);
    private String tenantId;
    private String uei = "uei.opennms.org/generic/traps/SNMP_Cold_Start";
    private final List<Event.Builder> builders = new ArrayList<>();
    private final KafkaTestHelper kafkaTestHelper;
    private final BackgroundSteps background;
    ThresholdAlertData.Builder thresholdAlertDataBuilder;

    @Given("[Event] External GRPC Port in system property {string}")
    public void externalGRPCPortInSystemProperty(String propertyName) {
        backgroundHelper.externalGRPCPortInSystemProperty(propertyName);
    }

    @Given("[Event] Grpc TenantId {string}")
    public void grpcTenantId(String tenantId) {
        this.tenantId = tenantId;
        backgroundHelper.grpcTenantId(tenantId);
    }

    @Given("[Event] Create Grpc Connection for Events")
    public void createGrpcConnectionForEvents() {
        backgroundHelper.createGrpcConnectionForEvents();
    }

    @Then("verify there are {int} events")
    public void verifyThereAreEvents(int arg0) {
        if (arg0 < 0) {
            fail("Events can't be in negative");
        } else {
            assertEquals(arg0, backgroundHelper.getEventCount());
        }
    }

    @When("Send Trap Data to Kafka Listener via Producer with TenantId {string} and Location {string}")
    public void sendTrapDataToKafkaListenerViaProducerWithTenantIdAndLocationId(String tenantId, String location) {
        TenantLocationSpecificTrapLogDTO tenantLocationSpecificTrapLogDTO =
                TenantLocationSpecificTrapLogDTO.newBuilder()
                        .setTenantId(tenantId)
                        .setLocationId(location)
                        .setTrapAddress("127.0.0.2")
                        .addTrapDTO(TrapDTO.newBuilder()
                                .setAgentAddress("127.0.0.2")
                                .build())
                        .build();
        var producerRecord = new ProducerRecord<String, byte[]>(
                backgroundHelper.getTopic(), tenantLocationSpecificTrapLogDTO.toByteArray());

        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, backgroundHelper.getBootstrapServer());
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        try (KafkaProducer<String, byte[]> kafkaProducer = new KafkaProducer<>(producerConfig)) {
            producerRecord.headers().add(GrpcConstants.TENANT_ID_KEY, tenantId.getBytes());
            kafkaProducer.send(producerRecord);
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    @Then("Check If There are any events")
    public void checkIfThereAreAnyEvents() {
        EventsSearchBy searchEventByLocationName = EventsSearchBy.newBuilder().build();
        await().atMost(15, TimeUnit.SECONDS)
                .pollDelay(3, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(
                        () ->
                                backgroundHelper
                                        .getEventServiceBlockingStub()
                                        .searchEvents(searchEventByLocationName)
                                        .getEventsList()
                                        .stream()
                                        .anyMatch(event -> event.getTenantId().equals(this.tenantId)
                                                && event.getUei().equals(uei)),
                        Matchers.is(true));
        List<Event> eventList = backgroundHelper
                .getEventServiceBlockingStub()
                .searchEvents(searchEventByLocationName)
                .getEventsList();
        assertTrue(eventList.stream()
                .anyMatch(event -> event.getTenantId().equals(this.tenantId)
                        && event.getUei().equals(uei)));
    }

    @Given("Initialize Trap Producer With Topic {string} and BootstrapServer {string}")
    public void initializeTrapProducerWithTopicAndBootstrapServer(String topic, String bootstrapServer) {
        backgroundHelper.initializeTrapProducer(topic, bootstrapServer);
    }

    @Given("[Event] data")
    public void nodeData(DataTable data) {
        for (Map<String, String> map : data.asMaps()) {
            Event.Builder builder = Event.newBuilder();
            builder.setTenantId(map.get("tenant_id"))
                    .setUei(map.get("uei"))
                    .setIpAddress(map.get("ip_address"))
                    .setLocationId(map.get("location_id"))
                    .setNodeId(map.containsKey("node_id") ? Long.parseLong(map.get("node_id")) : 0l)
                    .build();

            builders.add(builder);
        }
    }

    @When("Send events message to Kafka topic {string} and tenant {string}")
    public void sendEventsMessageToKafkaTopic(String topic, String tenantId) {
        List<Event> eventList = new ArrayList<>();
        for (Event.Builder builder : builders) {
            eventList.add(builder.build());
        }

        EventLog eventLog = EventLog.newBuilder()
                .setTenantId(tenantId)
                .addAllEvents(eventList)
                .build();

        kafkaTestHelper.sendToTopic(background.getEventTopic(), eventLog.toByteArray(), tenantId);
    }

    @Then("verify events persisted with uei {string} with total rows {int}")
    public void verifyEventsPersistedWithTenant(String uei, int rows) {
        await().atMost(20, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(
                        () ->
                                backgroundHelper
                                        .getEventServiceBlockingStub()
                                        .listEvents(Empty.newBuilder().build())
                                        .getEventsList()
                                        .stream()
                                        .anyMatch(event -> event.getUei().equals(uei)),
                        Matchers.is(true));
        List<Event> eventList =
                backgroundHelper
                        .getEventServiceBlockingStub()
                        .listEvents(Empty.newBuilder().build())
                        .getEventsList()
                        .stream()
                        .filter(event -> event.getUei().equals(uei))
                        .toList();
        assertNotNull(eventList);
        assertTrue(eventList.size() == rows);
    }

    @Given("prepare threshold request with below data when the threshold alert status is {string}")
    public void prepareThresholdRequestWithBelowDataWhenTheThresholdAlertStatusIs(
            String thresholdType, DataTable dataTable) {
        thresholdAlertDataBuilder = ThresholdAlertData.newBuilder().setTenantId(tenantId);
        List<ThresholdAlert> thresholdAlerts = dataTable.asMaps().stream()
                .map(map -> {
                    ThresholdAlert.Builder alertBuilder = ThresholdAlert.newBuilder();
                    map.forEach(alertBuilder::putLabels);
                    alertBuilder.setStatus(thresholdType).build();
                    return alertBuilder.build();
                })
                .toList();
        thresholdAlertDataBuilder.addAllAlerts(thresholdAlerts);
        thresholdAlertDataBuilder.build();
    }

    @Then("Send threshold request to Kafka topic")
    public void sendThresholdRequestToKafkaTopic() {
        kafkaTestHelper.sendToTopic(
                background.getThresholdTopic(),
                thresholdAlertDataBuilder.build().toByteArray(),
                tenantId);
    }
}
