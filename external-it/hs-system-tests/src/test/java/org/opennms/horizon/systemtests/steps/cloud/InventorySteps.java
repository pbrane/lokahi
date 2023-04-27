/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
package org.opennms.horizon.systemtests.steps.cloud;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.opennms.horizon.systemtests.pages.cloud.InventoryPage;

public class InventorySteps {

    @Then("Find discovered node with ip {string}")
    public void findDiscoveredNodeWithIp(String ipAddress) {
        InventoryPage.findCardWithIp(ipAddress);
    }

    @Then("Remove discovered node with ip {string}")
    public void removeDiscoveredNodeWithIp(String ipAddress) {
        InventoryPage.clickOnDeleteInventoryNode(ipAddress);
    }

    @When("Confirm node delete pop up has ip {string}")
    public void confirmNodeDeletePopUpHasIp(String ipAddress) {
        InventoryPage.verifyNodeDeletionPopUp(ipAddress);
    }

    @Then("Click on 'Delete' to remove the discovered node")
    public void clickOnDeleteToRemoveTheDiscoveredNode() {
        InventoryPage.clickOnConfirmNodeDeletion();
    }
}
