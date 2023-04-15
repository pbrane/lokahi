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

package org.opennms.horizon.systemtests;

import com.codeborne.selenide.Selenide;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.opennms.horizon.systemtests.pages.portal.PortalCloudPage;
import org.opennms.horizon.systemtests.pages.portal.PortalLoginPage;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    plugin = {"pretty",
        "json:cucumber.reports/cucumber-report.json",
        "html:cucumber.reports/cucumber-report.html"},
    tags = "@cloud"
)
public class HSCucumberRunnerTest {

    @Before("@portal")
    public static void loginToPortal() {
        if (Selenide.webdriver().driver().hasWebDriverStarted()) {
            return;
        }
        Selenide.open("https://dev.cloud.opennms.com");
        PortalLoginPage.closeCookieHeader();
        PortalLoginPage.setUsername(System.getProperty("portal_user.email"));
        PortalLoginPage.clickNext();
        PortalLoginPage.setPassword(System.getProperty("portal_user.password"));
        PortalLoginPage.clickSignIn();

        PortalCloudPage.verifyThatUserLoggedIn();
    }

    @After("@portal")
    public static void returnToPortalMainPage() {
        Selenide.open("https://dev.cloud.opennms.com/cloud");
    }

}
