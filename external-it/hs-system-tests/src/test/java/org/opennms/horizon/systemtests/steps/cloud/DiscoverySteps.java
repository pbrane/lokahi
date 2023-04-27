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
import org.opennms.horizon.systemtests.pages.cloud.DiscoveryPage;
import testcontainers.SnmpdContainer;

public class DiscoverySteps {

    @Then("Click on add discovery button")
    public void clickOnAddDiscoveryButton() {
        DiscoveryPage.clickOnAddDiscoveryButton();
    }

    @Then("Start SNMP docker image")
    public void startSNMPDockerImage() {
        SnmpdContainer.createNewSnmpdContainer();
    }

    @Then("Click on ICMP Active discovery radio button")
    public void clickOnICMPActiveDiscoveryRadioButton() {
        DiscoveryPage.clickOnICMPRadioButton();
    }

    @Then("Verify that the name is required")
    public void verifyThatTheNameIsRequired() {
        DiscoveryPage.icmpNameInputErrorMessage();
    }

    @Then("Verify that the ip address is required")
    public void verifyThatTheIpAddressIsRequired() {
        DiscoveryPage.ipInputShouldHaveErrorMessage();
    }

    @Then("Verify that tags field has {string} tag")
    public void verifyThatTagsFieldHasDefaultTag(String tag) {
        DiscoveryPage.shouldHaveDefaultTag(tag);
    }

    @Then("Verify that the location has minion location")
    public void verifyThatTheLocationHasMinionLocation() {
        DiscoveryPage.shouldHaveLocationFilled();
    }

    @Then("Verify that UDP port has {string} entered")
    public void verifyThatUDPPortHasEntered(String port) {
        DiscoveryPage.checkUdpPortDefaultInput(port);
    }

    @Then("Add an ICMP SNMP discovery name {string}")
    public void addAnICMPSNMPDiscoveryName(String name) {
        DiscoveryPage.setIcmpNameInput(name);
    }

    @Then("Add additional tag {string}")
    public void addAdditionalTag(String tag) {
        DiscoveryPage.setNewTagForDiscovery(tag);
    }

    @Then("Click on 'NEW' button to add a new tag")
    public void clickOnNewButtonToAddANewTag() {
        DiscoveryPage.clickOnNewTagButton();
    }

    @Then("Add device IP address range {string}")
    public void addDeviceIPAddress(String ipRange) {
        DiscoveryPage.setIpAddressInput(ipRange);
    }

    @Then("Verify that Community has {string} entered")
    public void verifyThatCommunityHasEntered(String community) {
        DiscoveryPage.checkCommunityDefaultInput(community);
    }

    @Then("Click on 'Save Discovery' button")
    public void clickOnSaveDiscoveryButton() {
        DiscoveryPage.clickOnSaveDiscoveryButton();
    }

    @Then("Click on 'Cancel' button")
    public void clickOnCancelButton() {
        DiscoveryPage.clickOnCancelButton();
    }

    @Then("Click on 'View Detected Nodes' button")
    public void clickOnViewDetectedNodesButton() {
        DiscoveryPage.clickOnViewDetectedNodesButton();
    }
}
