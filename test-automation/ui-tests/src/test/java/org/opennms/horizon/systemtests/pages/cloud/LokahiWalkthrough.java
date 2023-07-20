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
import org.opennms.horizon.systemtests.steps.MinionSteps;
import org.openqa.selenium.By;
import testcontainers.MinionContainer;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;

import java.time.Duration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LokahiWalkthrough {
    private static final SelenideElement startSetupBtn = $(By.xpath("//button[@data-test='welcome-slide-one-setup-button']"));
    private static final SelenideElement downloadBundleBtn = $(By.xpath("//button[@data-test='welcome-slide-two-download-button']"));
    private static final SelenideElement dockerCmd = $(By.xpath("//div[@class='welcome-slide-table-body']/textarea"));
    private static final SelenideElement minionDetectedCheck = $(By.xpath("//div[text()='Minion detected.']"));

    public static boolean containsWalkthroughButton() {
        return startSetupBtn.exists();
    }

    public static void waitOnWalkthroughOrMain() {
        $(By.xpath("//button[@data-test='welcome-slide-one-setup-button']|//div[@class='app-aside']")).should(exist);
    }

    public static MinionContainer addMinionUsingWalkthrough(String minionName) {
        try {
            File bundle = downloadBundleBtn.shouldBe(enabled).download();
            if (bundle.exists()) {
                // Parse out the pwd for the bundle
                String dockerText = dockerCmd.shouldBe(visible).getAttribute("value"); // getText doesn't work for textarea
                Pattern pattern = Pattern.compile("GRPC_CLIENT_KEYSTORE_PASSWORD='([a-z,0-9,-]*)'");
                Matcher matcher = pattern.matcher(dockerText);

                if (matcher.find()) {
                    MinionContainer minion = MinionSteps.startMinion(bundle, matcher.group(1), minionName);
                    // Minion startup and connect is slow - need a specific timeout here
                    minionDetectedCheck.should(exist, Duration.ofSeconds(60));
                    return minion;
                }
                fail("Unable to parse p12 password from docker string: " + dockerText);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        fail("Failure downloading p12 bundle file");
        return null;
    }

    public static void startSetup() {
        startSetupBtn.shouldBe(enabled).click();
    }
}
