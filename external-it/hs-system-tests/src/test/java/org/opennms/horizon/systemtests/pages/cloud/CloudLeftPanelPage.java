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

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class CloudLeftPanelPage {
    private static final SelenideElement applianceLnk = $("[href='/appliances']");
    private static final SelenideElement leftPanel = $(".app-aside");

    public static void verifyLeftPanelIsDisplayed() {
        leftPanel.shouldBe(visible);
    }

    public static void clickOnApplianceSection() {
        applianceLnk.shouldBe(Condition.enabled).click();
    }

    public static void clickOnPanelSection(String section) {
        $("div.feather-app-rail").shouldBe(visible, Duration.ofSeconds(10)).hover().shouldNotHave(Condition.cssClass("narrow"));
        SelenideElement menuOption = $(String.format("[href='/%s']", section)).shouldBe(enabled);
        menuOption.click();
        menuOption.shouldHave(cssClass("selected"));
        $("div.right").click();
    }
}
