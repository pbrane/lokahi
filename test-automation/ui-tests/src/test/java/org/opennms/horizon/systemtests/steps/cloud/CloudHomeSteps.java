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

import com.codeborne.selenide.Selenide;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.opennms.horizon.systemtests.pages.cloud.CloudHomePage;
import org.opennms.horizon.systemtests.pages.cloud.CloudLeftPanelPage;

public class CloudHomeSteps {
    @Then("sees {string} subtitle in the Top 10 Applications chart")
    public void verifyErrorMessage(String subtitle) {
        CloudLeftPanelPage.clickOnPanelSection("home");
        CloudHomePage.verifyTop10ApplicationsSubtitle(subtitle);
    }

    @Then("wait until the 'Top 10 Applications' chart will reflect the received data")
    public void waitFlowsChart() {
        for (int i = 0; i < 6; i++) {
            Selenide.sleep(5_000);
            Selenide.refresh();
            if (CloudHomePage.verifyTop10Applications()) {
                break;
            }
        }
        CloudHomePage.verifyNoDataTop10ApplicationsState(false);
    }

    @Then("click on 'Flows' link")
    public void clickOnFlows() {
        CloudHomePage.clickOnFlowsLink();
    }
}
