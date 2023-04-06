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

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.opennms.horizon.systemtests.utils.Locators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class LoginSteps {

    private static final Logger LOG = LoggerFactory.getLogger(LoginSteps.class);

    private String cloudBaseUrl;
    private String cloudUsername;
    private String cloudPassword;

    @Given("Login to the web with provided login details")
    public void loginToTheWebInterfaceWithProvidedAnd() {
        doLogin();
    }

    @Then("Verify that we logged in successfully")
    public void verifyThatWeLoggedInSuccessfully() {
        LOG.info("waiting for the product icon");

        $(Locators.OPENNMS_BANNER).shouldBe(visible, Duration.ofMinutes(2));
    }

    @Given("Cloud url in environment variable {string}")
    public void cloudUrlInEnvironmentVariable(String variableName) {
        cloudBaseUrl = System.getenv(variableName);

        LOG.info("CLOUD BASE URL: {}", cloudBaseUrl);
    }

    @Given("Cloud username in environment variable {string}")
    public void cloudUsernameInEnvironmentVariable(String variableName) {
        cloudUsername = System.getenv(variableName);

        LOG.info("CLOUD USERNAME: {}", cloudUsername);
    }

    @Given("Cloud password in environment variable {string}")
    public void cloudPasswordInEnvironmentVariable(String variableName) {
        cloudPassword = System.getenv(variableName);
    }

    /**
     * General method to log in to web portal
     */
    private void doLogin() {
        LOG.info("doLogin method started");
        // Open browser with Selenide. Browser configuration in selenide.properties file
        open(cloudBaseUrl);
        // Wait if login appears
        $(Locators.LOGIN_USERNAME).shouldBe(visible, Duration.ofMinutes(2)).sendKeys(cloudUsername);
        $(Locators.LOGIN_NEXT).click();
        // Wait for the password appears
        $(Locators.LOGIN_PASSWORD).shouldBe(visible, Duration.ofMinutes(2)).sendKeys(cloudPassword);
        $(Locators.LOGIN_SUBMIT).click();
    }
}
