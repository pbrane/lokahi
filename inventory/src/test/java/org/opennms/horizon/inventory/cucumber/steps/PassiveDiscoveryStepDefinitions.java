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

package org.opennms.horizon.inventory.cucumber.steps;

import com.google.protobuf.Empty;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.opennms.horizon.inventory.cucumber.InventoryBackgroundHelper;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryCreateDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryResponseDTO;
import org.opennms.horizon.inventory.dto.PassiveDiscoveryServiceGrpc;
import org.opennms.horizon.inventory.dto.TagCreateDTO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PassiveDiscoveryStepDefinitions {
    private static InventoryBackgroundHelper backgroundHelper;
    private PassiveDiscoveryCreateDTO passiveDiscoveryCreateDTO;
    private PassiveDiscoveryDTO createdDiscovery;
    private PassiveDiscoveryResponseDTO fetchedDiscoveryResponse;

    @BeforeAll
    public static void beforeAll() {
        backgroundHelper = new InventoryBackgroundHelper();
    }

    /*
     * BACKGROUND GIVEN
     * *********************************************************************************
     */
    @Given("[Passive] External GRPC Port in system property {string}")
    public void externalGRPCPortInSystemProperty(String propertyName) {
        backgroundHelper.externalGRPCPortInSystemProperty(propertyName);
    }

    @Given("[Passive] Kafka Bootstrap URL in system property {string}")
    public void kafkaBootstrapURLInSystemProperty(String systemPropertyName) {
        backgroundHelper.kafkaBootstrapURLInSystemProperty(systemPropertyName);
    }

    @Given("[Passive] Grpc TenantId {string}")
    public void grpcTenantId(String tenantId) {
        backgroundHelper.grpcTenantId(tenantId);
    }

    @Given("[Passive] Create Grpc Connection for Inventory")
    public void createGrpcConnectionForInventory() {
        backgroundHelper.createGrpcConnectionForInventory();
    }

    /*
     * SCENARIO GIVEN
     * *********************************************************************************
     */
    @Given("Passive Discovery fields to persist")
    public void passiveDiscoveryFieldsToPersist() {
        passiveDiscoveryCreateDTO = PassiveDiscoveryCreateDTO.newBuilder()
            .addLocations("Default")
            .addCommunities("public")
            .addPorts(161)
            .addTags(TagCreateDTO.newBuilder().setName("tag-name").build())
            .build();
    }

    /*
     * SCENARIO WHEN
     * *********************************************************************************
     */
    @When("A GRPC request to create a new passive discovery")
    public void aGRPCRequestToCreateANewPassiveDiscovery() {
        PassiveDiscoveryServiceGrpc.PassiveDiscoveryServiceBlockingStub stub
            = backgroundHelper.getPassiveDiscoveryServiceBlockingStub();
        createdDiscovery = stub.createDiscovery(passiveDiscoveryCreateDTO);
    }

    @And("A GRPC request to get passive discovery")
    public void aGRPCRequestToGetPassiveDiscovery() {
        PassiveDiscoveryServiceGrpc.PassiveDiscoveryServiceBlockingStub stub
            = backgroundHelper.getPassiveDiscoveryServiceBlockingStub();
        fetchedDiscoveryResponse = stub.getDiscovery(Empty.getDefaultInstance());
    }

    /*
     * SCENARIO THEN
     * *********************************************************************************
     */
    @Then("The creation and the get of passive discovery should be the same")
    public void theCreationAndTheGetOfPassiveDiscoveryShouldBeTheSame() {

        assertTrue(fetchedDiscoveryResponse.hasDiscovery());
        PassiveDiscoveryDTO fetchedDiscovery = fetchedDiscoveryResponse.getDiscovery();

        assertEquals(passiveDiscoveryCreateDTO.getLocationsCount(), createdDiscovery.getLocationsCount());
        assertEquals(passiveDiscoveryCreateDTO.getLocationsCount(), fetchedDiscovery.getLocationsCount());
        assertEquals(passiveDiscoveryCreateDTO.getLocations(0), createdDiscovery.getLocations(0));
        assertEquals(passiveDiscoveryCreateDTO.getLocations(0), fetchedDiscovery.getLocations(0));

        assertEquals(passiveDiscoveryCreateDTO.getPortsCount(), createdDiscovery.getPortsCount());
        assertEquals(passiveDiscoveryCreateDTO.getPortsCount(), fetchedDiscovery.getPortsCount());
        assertEquals(passiveDiscoveryCreateDTO.getPorts(0), createdDiscovery.getPorts(0));
        assertEquals(passiveDiscoveryCreateDTO.getPorts(0), fetchedDiscovery.getPorts(0));

        assertEquals(passiveDiscoveryCreateDTO.getCommunitiesCount(), createdDiscovery.getCommunitiesCount());
        assertEquals(passiveDiscoveryCreateDTO.getCommunitiesCount(), fetchedDiscovery.getCommunitiesCount());
        assertEquals(passiveDiscoveryCreateDTO.getCommunities(0), createdDiscovery.getCommunities(0));
        assertEquals(passiveDiscoveryCreateDTO.getCommunities(0), fetchedDiscovery.getCommunities(0));
    }
}
