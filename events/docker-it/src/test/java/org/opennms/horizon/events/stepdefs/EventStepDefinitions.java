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
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.opennms.horizon.events.EventsBackgroundHelper;
import org.opennms.horizon.inventory.dto.MonitoringLocationCreateDTO;
import org.opennms.horizon.inventory.dto.NodeCreateDTO;
import org.opennms.horizon.inventory.dto.NodeDTO;

@RequiredArgsConstructor
@Slf4j
public class EventStepDefinitions {
    private EventsBackgroundHelper backgroundHelper;

    private NodeDTO node;

    public EventStepDefinitions(EventsBackgroundHelper backgroundHelper) {
        this.backgroundHelper = backgroundHelper;
    }

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
        backgroundHelper.sendTrapDataToKafkaListenerViaProducerWithTenantIdAndLocationId(
                tenantId, /*backgroundHelper.findLocationId(location)*/ location);
    }

    @Then("Check If There are {int} Events with NodeId {int} and Location {string}")
    public void checkIfThereAreEventsWithNodeIdAndLocation(int eventsCount, int nodeId, String location) {
        backgroundHelper.searchEventWithLocation(eventsCount, Integer.getInteger(node.getId() + ""), location);
    }

    @Given("Initialize Trap Producer With Topic {string} and BootstrapServer {string}")
    public void initializeTrapProducerWithTopicAndBootstrapServer(String topic, String bootstrapServer) {
        backgroundHelper.initializeTrapProducer(topic, bootstrapServer);
    }

    @Given("[Common] Create {string} Location")
    public void createLocation(String location) {
        var locationServiceBlockingStub = backgroundHelper.getMonitoringLocationStub();
        try {
            var locationDto = locationServiceBlockingStub.createLocation(MonitoringLocationCreateDTO.newBuilder()
                    .setLocation(location)
                    .build());
            Assertions.assertNotNull(locationDto);
        } catch (StatusRuntimeException e) {
            // catch duplicate location
        }
    }

    @When("Add a device with IP address = {string} with label {string} and location {string}")
    public void addADeviceWithIPAddressWithLabelAndLocation(String ipAddress, String label, String location) {
        var nodeServiceBlockingStub = backgroundHelper.getNodeServiceBlockingStub();
        node = nodeServiceBlockingStub.createNode(NodeCreateDTO.newBuilder()
                .setLabel(label)
                .setLocationId(location)
                .setManagementIp(ipAddress)
                .build());
        assertNotNull(node);
    }
}
