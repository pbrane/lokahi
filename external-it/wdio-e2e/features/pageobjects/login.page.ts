import { ChainablePromiseElement } from 'webdriverio';

import Page from './page';

/**
 * sub page containing specific selectors and methods for a specific page
 */
class LoginPage extends Page {
    /**
     * define selectors using getter methods
     */
    public get inputUsername () {
        //return $('#username');
        return $('#idp-discovery-username');
    }

    public get inputPassword () {
        //return $('#password');
        return $('#okta-signin-password');
    }

    public get btnNext () {
        return $('#idp-discovery-submit');
    }

    public get btnSubmit () {
        //return $('button[type="submit"]');
        return $('#okta-signin-submit');
    }

    /**
     * a method to encapsule automation code to interact with the page
     * e.g. to login using username and password
     */
    public async login (username: string, password: string) {
        await this.inputUsername.setValue(username);
        await this.inputPassword.setValue(password);
        await this.btnSubmit.click();
    }

    public async loginCloud (username: string, password: string) {
        await this.inputUsername.setValue(username);
        await this.btnNext.click();

        await this.inputPassword.setValue(password);
        await this.btnSubmit.isClickable;
        await this.btnSubmit.click();
    }

    /**
     * overwrite specific options to adapt it to page object
     */
    public open () {
        return super.open('login');
    }
}

export default new LoginPage();
