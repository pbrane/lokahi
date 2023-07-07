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

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.FileDownloadMode;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.SneakyThrows;
import org.junit.Assert;
import testcontainers.DockerComposeMinionContainer;
import java.io.File;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.$;

public class WelcomePage {

    private static final SelenideElement startSetupButton = $("[data-test='welcome-slide-one-setup-button']");
    private static final SelenideElement downloadCertificateButton = $("[data-test='welcome-slide-two-download-button']");
    private static final SelenideElement dockerRunCLTextField =  $(withText("GRPC_CLIENT_KEYSTORE_PASSWORD"));
    private static final SelenideElement minionStatusField =  $("[data-test='welcome-minion-status-txt']");
    private static final SelenideElement continueButton =  $("[data-id='welcome-slide-two-continue-button']");
    private static final SelenideElement discoveryIPField =  $(byXpath("/html/body/div/div/div[3]/div/div/div/div[2]/div/div[4]/div/div[2]/div[2]/div[1]/div[2]/input"));
    private static final SelenideElement startDiscoveryButton =  $("[data-test='welcome-store-page-three-start-discovery-button']");
    private static final SelenideElement discoveryLoadingField =  $("[data-test='welcome-discovery-status-txt']");
    private static final SelenideElement discoveryResultNodeIPField =  $(byXpath("/html/body/div/div/div[3]/div/div/div/div[2]/div/div[4]/div/div[3]/div[2]/div/div[1]/strong"));
    private static final SelenideElement discoveryResultNodeStatusField =  $(byXpath("/html/body/div/div/div[3]/div/div/div/div[2]/div/div[4]/div/div[3]/div[2]/div/div[2]/div[1]/div"));
    private static final SelenideElement discoveryResultNodeICMPField =  $(byXpath("/html/body/div/div/div[3]/div/div/div/div[2]/div/div[4]/div/div[3]/div[2]/div/div[2]/div[2]/div"));
    private static final SelenideElement discoveryFinalContinueButton =  $(byXpath("/html/body/div/div/div[3]/div/div/div/div[2]/div/div[4]/div/div[4]/button[2]/span"));

    public static void checkIsStartSetupButtonVisible() {
        startSetupButton.shouldBe(enabled);
    }

    @SneakyThrows
    public static void startWelcomeWizardSetup() {
        startSetupButton.shouldBe(enabled).click();
    }

    @SneakyThrows
    public static void downloadCertificateAndStartMinion(String minionID) {
        Configuration.fileDownload = FileDownloadMode.FOLDER;
        File cert = downloadCertificateButton.shouldBe(enabled).download(60000);
        Assert.assertTrue(cert.exists());

        String dockerCL = null;
        for (int i = 0; i < 10; i++) {
            Selenide.sleep(3000);
            dockerCL = dockerRunCLTextField.getText();
            if (dockerCL.indexOf("GRPC_CLIENT_KEYSTORE_PASSWORD='") > 0) {
                break;
            }
        }

        int start = dockerCL.indexOf("GRPC_CLIENT_KEYSTORE_PASSWORD='") + "GRPC_CLIENT_KEYSTORE_PASSWORD='".length();
        int end = dockerCL.indexOf("' -e MINION_ID=");
        Assert.assertTrue (end > start && start > 0);

        String password = dockerCL.substring(start, end);
        DockerComposeMinionContainer.createNewContainer("src/test/resources/docker-compose-cloud.yaml", cert, minionID, password);
    }

    public static void checkMinionConnection() {
        for (int i = 0; i < 10; i++) {
            Selenide.sleep(5000);
            if (!"Please wait while we detect your Minion.".equals(minionStatusField.getText())) {
                break;
            }
        }
        Assert.assertEquals("Expected to see 'Minion detected.'", "Minion detected.", minionStatusField.getText());
    }

    public static void continueToDiscovery() {
        continueButton.shouldBe(enabled).click();
    }

    public static void setIPForDiscovery(String ip) {
        discoveryIPField.shouldBe(enabled).val("");
        discoveryIPField.sendKeys(ip);
    }

    public static void clickStartDiscovery() {
        startDiscoveryButton.shouldBe(enabled).click();
    }

    public static void nodeDiscovered(String ip) {
        try {
            for (int i = 0; i < 50; i++) {
                Selenide.sleep(3000);
                if (!"Loading first discovery.".equals(discoveryLoadingField.getText())) {
                    break;
                }
            }
        }
        catch (com.codeborne.selenide.ex.ElementNotFound e) {
            Assert.assertEquals(ip, discoveryResultNodeIPField.getText());
            Assert.assertEquals("Minion status should be 'UP'", "UP", discoveryResultNodeStatusField.getText());
            Assert.assertTrue("ICMP should be less then 500",500 >= Double.valueOf(discoveryResultNodeICMPField.getText()));
        }
    }

    public static void clickContinueToEndWizard() {
        discoveryFinalContinueButton.shouldBe(enabled).click();
    }
}
