import { Given, When, Then } from '@wdio/cucumber-framework';

import LoginPage from '../pageobjects/login.page';
import WebConsole from '../pageobjects/webconsole.page';

const pages = {
    login: LoginPage
}

Given(/^I am on the (\w+) page$/, async (page) => {
    await pages[page].open()
});

When(/^I login with (\w+) and (.+)$/, async (username, password) => {
    await LoginPage.login(username, password)
});

Then(/^I should see a welcome message (.*)$/, async (message) => {
    await expect(WebConsole.welcomeHeader).toBeExisting();
    await expect(WebConsole.welcomeHeader).toHaveTextContaining(message);
});

