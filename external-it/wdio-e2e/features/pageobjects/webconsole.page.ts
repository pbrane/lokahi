import { ChainablePromiseElement } from 'webdriverio';

import Page from './page';

/**
 * sub page containing specific selectors and methods for a specific page
 */
class WebConsolePage extends Page {
    /**
     * define selectors using getter methods
     */
    public get welcomeHeader () {
        return $('div[class="welcome"]');
    }


    public get btnAddDevice () {
        return $('button[data-test="add-node-btn"]');
    }

    public get dialogWindowAddDevice () {
        return $('div[class="dialog-body"]');
    }

    public get inputName () {
        return $('input[data-test="name-input"]');
    }

    public get inputLocation () {
        return $('input[class="feather-select-input"]');
    }

    public get inputIpAddress () {
        return $('input[data-test="ip-input"]');
    }

    public get btnSave () {
        return $('button[type="button"]');
    }


    public async addDevice (name: string, location: string, ipAddress: string) {
        await this.inputName.setValue(name);
        await this.inputLocation.setValue(location);
        await this.inputIpAddress.setValue(ipAddress);
        await this.btnSave.click();
    }

}

export default new WebConsolePage();
