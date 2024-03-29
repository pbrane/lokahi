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

import static org.junit.jupiter.api.Assertions.*;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.horizon.events.EventsBackgroundHelper;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.grpc.traps.contract.TenantLocationSpecificTrapLogDTO;
import org.opennms.horizon.grpc.traps.contract.TrapDTO;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class EventStepDefinitions {
    private final EventsBackgroundHelper backgroundHelper;
    private static final Logger LOG = LoggerFactory.getLogger(EventStepDefinitions.class);

    @Given("[Event] External GRPC Port in system property {string}")
    public void externalGRPCPortInSystemProperty(String propertyName) {
        backgroundHelper.externalGRPCPortInSystemProperty(propertyName);
    }

    @Given("[Event] Grpc TenantId {string}")
    public void grpcTenantId(String tenantId) {
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
        /*var locationServiceBlockingStub = backgroundHelper.getMonitoringLocationStub();
        String locationId = null;
        try {
            locationId =
                    locationServiceBlockingStub.listLocations(Empty.newBuilder().build()).getLocationsList().stream()
                            .filter(loc -> location.equals(loc.getLocation()))
                            .findFirst()
                            .map(MonitoringLocationDTO::getId)
                            .map(String::valueOf)
                            .orElseThrow(() -> new IllegalArgumentException("Location " + location + " not found"));
            assertNotNull(locationId);
            System.out.println("Location Id" + locationId);
            LOG.info("Location {} Location Id {}", location, locationId);
        } catch (StatusRuntimeException e) {
            LOG.error(e.toString());
        }*/
        /*TenantLocationSpecificTrapLogDTO tenantLocationSpecificTrapLogDTO =
        TenantLocationSpecificTrapLogDTO.newBuilder()
                //.setLocationId(this.locationId)
                .setTenantId(tenantId)
                .build();*/
        TenantLocationSpecificTrapLogDTO tenantLocationSpecificTrapLogDTO =
                TenantLocationSpecificTrapLogDTO.newBuilder()
                        .setTenantId(tenantId)
                        .setLocationId(location)
                        .setTrapAddress("127.0.0.1")
                        .addTrapDTO(TrapDTO.newBuilder()
                                .setAgentAddress("127.0.0.1")
                                .build())
                        .build();
        LOG.info("Trap data {}", tenantLocationSpecificTrapLogDTO.toString());
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

    @Then("Check If There are {int} Events with Location {string}")
    public void checkIfThereAreEventsWithLocation(int eventsCount, String location) {
        List<Event> searchEvents = backgroundHelper.searchEventWithLocation(eventsCount, 1, location);

        assertNotNull(searchEvents);
        assertEquals(eventsCount, searchEvents.size());
    }

    @Given("Initialize Trap Producer With Topic {string} and BootstrapServer {string}")
    public void initializeTrapProducerWithTopicAndBootstrapServer(String topic, String bootstrapServer) {
        backgroundHelper.initializeTrapProducer(topic, bootstrapServer);
    }

    @Given("Tenant id {string}")
    @Given("Tenant {string}")
    @Given("A new tenant named {string}")
    public String useSpecifiedTenantId(String tenantId) {
        return backgroundHelper.useSpecifiedTenantId(tenantId);
    }
}
