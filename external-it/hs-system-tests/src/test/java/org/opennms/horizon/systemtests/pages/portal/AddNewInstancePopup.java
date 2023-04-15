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


package org.opennms.horizon.systemtests.pages.portal;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class AddNewInstancePopup {

    private static final SelenideElement popup = $("#add-instancemodal");
    private static final SelenideElement instanceNameInp = $("#cloud-add-instance-modal-name");
    private static final SelenideElement submitBtn = $("button.confirm-dialog-submit-button");
    private static final SelenideElement meOption = $x("//div[@data-ref-id='feather-radio' and contains(.,'Me')]");
    private static final SelenideElement anotherEmailInp = $("#cloud-add-instance-modal-email-with");
    private static final SelenideElement anotherEmailDropDown = $("div.feather-menu-dropdown .feather-list-item-text");
    private static final SelenideElement someoneElseOption = $x("//div[@data-ref-id='feather-radio' and contains(.,'Someone else')]");
    private static final SelenideElement cancelBtn = $("button.confirm-dialog-cancel-button");
    private static final SelenideElement closeBtn = $("a.closeButton");
    private static final SelenideElement errorMessageTxt = $("[data-ref-id='feather-form-element-error']");
    private static final SelenideElement emailErrorMessageTxt = $("[data-ref-id='feather-form-element-error']");


    public static void waitPopupIsDisplayed(boolean visible) {
        if (visible) {
            popup.shouldBe(Condition.visible);
        } else {
            popup.shouldBe(Condition.disappear);
        }
    }

    public static void setInstanceName(String instanceName) {
        instanceNameInp.setValue("");
        instanceNameInp.sendKeys(instanceName);
    }

    public static void clickSubmitBtn() {
        submitBtn.shouldBe(Condition.enabled).click();
    }

    public static void selectMeOption() {
        meOption.shouldBe(Condition.enabled).click();
    }

    public static void selectSomeoneElseOption() {
        someoneElseOption.shouldBe(Condition.enabled).click();
    }

    public static void clickCancel() {
        cancelBtn.shouldBe(Condition.enabled).click();
    }

    public static void clickCloseBtn() {
        closeBtn.shouldBe(Condition.enabled).click();
    }

    public static void verifyErrorMessageForInstanceName(String errorMessage) {
        errorMessageTxt.shouldHave(Condition.text(errorMessage));
    }

    public static void verifyNoErrorMessageForInstanceName() {
        errorMessageTxt.shouldBe(Condition.hidden);
    }

    public static void setAssignedUserEmail(String email) {
        anotherEmailInp.shouldBe(Condition.enabled).setValue("").sendKeys(email);
    }

    public static void confirmEmailInDropdown(String email) {
        anotherEmailDropDown.shouldHave(Condition.text(email), Condition.visible).click();
    }

    public static void verifyErrorMessageForEmailAddress(String errorMessage) {
        emailErrorMessageTxt.shouldHave(Condition.text(errorMessage));
    }

    public static void verifyNoErrorMessageForEmailAddress() {
        emailErrorMessageTxt.shouldBe(Condition.hidden);
    }
}
