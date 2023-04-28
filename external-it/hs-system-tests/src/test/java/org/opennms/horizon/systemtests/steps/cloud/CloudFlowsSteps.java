package org.opennms.horizon.systemtests.steps.cloud;

import io.cucumber.java.en.Then;
import org.opennms.horizon.systemtests.pages.cloud.CloudFlowsPage;

public class CloudFlowsSteps {
    @Then("sees 'No Data' in the flows table")
    public void verifyNoData() {
        CloudFlowsPage.verifyNoDataTitle();
    }

    @Then("sees chart for netflow data")
    public void verifyChartVisibility() {
        CloudFlowsPage.verifyChartVisibility();
    }

    @Then("click on 'Exporter' filter")
    public void clickOn() {
        CloudFlowsPage.clickOnExporterInput();
        CloudFlowsPage.checkDropdown();
    }
}
