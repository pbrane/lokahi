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

package org.opennms.horizon.systemtests.pages.cloud;

import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static org.opennms.horizon.systemtests.CucumberHooks.MINIONS;

public class DiscoveryPage {

    private static final SelenideElement addDiscoveryButton = $("section.my-discovery button");
    private static final SelenideElement ICMPRadio = $x("//span[text()='ICMP/SNMP' and @data-ref-id='feather-radio-label']");
    private static final SelenideElement AzureRadio = $x("//span[text()='AZURE' and @data-ref-id='feather-radio-label']");
    private static final SelenideElement SyslogRadio = $x("//span[text()='Syslog & SNMP Traps' and @data-ref-id='feather-radio-label']");

    private static final SelenideElement icmpNameInput = $("div.name-input input");
    private static final SelenideElement icmpNameInputErrorMessage = $("div.name-input div.feather-input-error");

    private static final SelenideElement ipAddressRangeInput = $("div.ip-input div#contentEditable_1");
    private static final SelenideElement ipAddressRangeErrorMessage = $("div.ip-input div.errorMsgBox");

    private static final SelenideElement locationSelector = $("div.search-location textarea");
    private static final SelenideElement locationChip = $("div.search-location div.chip");

    private static final SelenideElement tagsInput = $("div.tags-autocomplete [data-ref-id='feather-autocomplete-input']");
    private static final SelenideElement tagsChip = $("div.tags-autocomplete div.chip");
    private static final SelenideElement tagsNewButton = $("span.autocomplete-item-new-label");

    private static final SelenideElement communityInput = $("div.community-input div#contentEditable_2");
    private static final SelenideElement udpPortsInput = $("div.udp-port-input div#contentEditable_3");

    private static final SelenideElement saveDiscoveryButton = $x("//button [@type='submit']");
    private static final SelenideElement viewDetectedNodesButton = $x("//button [text()=' View Detected Nodes']");
    private static final SelenideElement cancelButton = $("button.btn-secondary");

    public static void clickOnAddDiscoveryButton() {
        addDiscoveryButton.shouldBe(enabled, Duration.ofSeconds(7)).click();
    }

    public static void clickOnICMPRadioButton() {
        ICMPRadio.shouldBe(enabled).click();
    }

    public static void icmpNameInputErrorMessage() {
        icmpNameInputErrorMessage.shouldHave(text("Name is required."));
    }

    public static void setIcmpNameInput(String name) {
        icmpNameInput.shouldBe(enabled).sendKeys(name);
    }

    public static void ipInputShouldHaveErrorMessage() {
        ipAddressRangeErrorMessage.shouldHave(text("The field is required."));
    }

    public static void shouldHaveLocationFilled() {
        locationChip.shouldHave(text(MINIONS.get(0).minionLocation));
    }

    public static void shouldHaveDefaultTag(String tag) {
        tagsChip.shouldHave(text(tag));
    }

    public static void setNewTagForDiscovery(String tag) {
        tagsInput.shouldBe(enabled).sendKeys(tag);
    }

    public static void clickOnNewTagButton() {
        tagsNewButton.shouldBe(enabled).click();
    }

    /**
     * Ip address range should be in format of a single ip address or over dash
     * like 192.168.0.1 - 192.168.0.255
     * @param ipRange Address range
     */
    public static void setIpAddressInput(String ipRange) {
        ipAddressRangeInput.shouldBe(enabled).sendKeys(ipRange);
    }

    public static void checkCommunityDefaultInput(String community) {
        communityInput.shouldHave(text(community));
    }

    /**
     * Community input be default is 'public'
     * @param community Community name
     */
    public static void setCommunityInput(String community) {
        communityInput.shouldBe(enabled).sendKeys(community);
    }

    public static void checkUdpPortDefaultInput(String port) {
        udpPortsInput.shouldHave(text(port));
    }

    /**
     * UDP ports range should be in format of a single port or comma separated
     * @param ports UDP ports
     */
    public static void setUdpPortInput(String ports) {
        udpPortsInput.shouldBe(enabled).sendKeys(ports);
    }

    public static void clickOnSaveDiscoveryButton() {
        saveDiscoveryButton.shouldBe(enabled).click();
    }

    public static void clickOnViewDetectedNodesButton() {
        viewDetectedNodesButton.shouldBe(enabled).click();
    }

    public static void clickOnCancelButton() {
        cancelButton.shouldBe(enabled).click();
    }

}
