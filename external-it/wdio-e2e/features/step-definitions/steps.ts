import { Given, When, Then } from '@wdio/cucumber-framework';

import LoginPage from '../pageobjects/login.page';
import WebConsole from '../pageobjects/webconsole.page';

const pages = {
    login: LoginPage,
    webconsole: WebConsole
}

Given(/^I am on the (\w+) page$/, async (page) => {
    await pages[page].open()
});

When('I login with {string} and {string}', async (username: string, password: string) => {
    await LoginPage.loginCloud(username, password)
});

Then('I should see a welcome message {string}', async (message: string) => {
    await expect(WebConsole.welcomeHeader).toBeExisting();
    await expect(WebConsole.welcomeHeader).toHaveTextContaining(message);
});

When('I see the button ADD DEVICE I can click on it', async () => {
    await expect(WebConsole.btnAddDevice).toBeExisting();
    await WebConsole.btnAddDevice.click();
});

When('I should see add device popup window', async () => {
    await expect(WebConsole.dialogWindowAddDevice).toBeExisting();
})


When('I add a new device with name {string} location {string} ipaddress {string}', async (nodeName: string, location: string, ipAddress: string) => {
  await WebConsole.addDevice(nodeName, location, ipAddress);
});



