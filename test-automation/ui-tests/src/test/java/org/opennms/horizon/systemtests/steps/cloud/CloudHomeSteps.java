package org.opennms.horizon.systemtests.steps.cloud;

import com.codeborne.selenide.Selenide;
import io.cucumber.java.en.Then;
import org.opennms.horizon.systemtests.pages.cloud.CloudHomePage;

public class CloudHomeSteps {
    @Then("sees {string} subtitle in the 'Top 10 Applications' chart")
    public void verifyErrorMessage(String subtitle) {
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
