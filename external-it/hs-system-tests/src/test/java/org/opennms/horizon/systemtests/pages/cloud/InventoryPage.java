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
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

public class InventoryPage {

    // TODO should be replaced with element after IDs are assigned HS-1569
    private static final String inventoryCardPath = "//h4[@data-test='heading' and text()='%s']/../../section[position()=2]";

    private static final SelenideElement nodeDeleteButton = $x("//button [@data-testid='save-btn']");

    public static void findCardWithIp(String ipAddress) {
        $x(String.format(inventoryCardPath, ipAddress)).shouldBe(visible, Duration.ofSeconds(30));
    }

    public static void clickOnDeleteInventoryNode(String ipAddress) {
        $x(String.format(inventoryCardPath, ipAddress))
            .shouldBe(enabled, Duration.ofSeconds(30))
            .findElement(By.cssSelector("[data-test='delete']"))
            .click();
    }

    public static void verifyNodeDeletionPopUp(String ipAddress) {
        $x(String.format("//header [@data-ref-id='feather-dialog-header' and text()='%s']", ipAddress)).shouldBe(visible);
    }

    public static void clickOnConfirmNodeDeletion() {
        nodeDeleteButton.shouldBe(enabled).click();
    }

}
