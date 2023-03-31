/**
* main page object containing all methods, selectors and functionality
* that is shared across all page objects
*/
export default class Page {
    /**
    * Opens a sub page of the page
    * @param path path of the sub page (e.g. /path/to/page.html)
    */
    public open (path: string) {
        // return browser.url("http://localhost:8123");
        return browser.url("https://7162ec9a-baa7-4a6c-81ca-505a9aee32ef.tnnt.onms-fb-dev.dev.nonprod.dataservice.opennms.com");
    }
}
