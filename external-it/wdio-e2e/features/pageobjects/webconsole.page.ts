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
}

export default new WebConsolePage();
