package org.opennms.horizon.systemtests.pages.cloud;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.hidden;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class CloudHomePage {
    private static final SelenideElement headerTxt = $("[data-test='page-headline']");
    private static final SelenideElement flowsCard = $x("//div[ @class='card' and .//div[text()='Top 10 Applications'] ]");
    private static final SelenideElement emptyFlowCard = $x("//div[ @class='card' and .//div[text()='Top 10 Applications'] ]//div[@class='empty']");
    private static final SelenideElement flowsCardSubtitle = $x("//div[ @class='card' and .//div[text()='Top 10 Applications']]//div[@class='subtitle']");
    private static final SelenideElement flowsLink = $x("//div[text()='Flows']");

    public static void verifyNoDataTop10ApplicationsState(boolean isVisible) {
        emptyFlowCard.shouldBe(isVisible ? visible : hidden);
    }

    public static void verifyTop10ApplicationsSubtitle(String subtitleTxt) {
        flowsCardSubtitle.shouldBe(text(subtitleTxt));
    }

    public static boolean verifyTop10Applications() {
        return flowsCard.find("canvas#pieChartApplications").isDisplayed();
    }

    public static void clickOnFlowsLink() {
        flowsLink.shouldBe(enabled).click();
        CloudFlowsPage.waitPageLoaded();
    }
}
