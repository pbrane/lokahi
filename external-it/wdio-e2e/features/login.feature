Feature: Loggin into the Horizon Stream using WebDriver

  Scenario: User log into the system

    Given I am on the login page
    When I login with "chadfieldqa@gmail.com" and "c4fRbzSy7tsgkg9"
    Then I should see a welcome message "Welcome"

     
  Scenario: Manually add a node to the inventory

    Given I am on the webconsole page
    When I see the button ADD DEVICE I can click on it 
    When I should see add device popup window
    When I add a new device with name "Node" location "Default" ipaddress "127.0.0.1"

