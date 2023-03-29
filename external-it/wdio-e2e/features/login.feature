Feature: Loggin into the Horizon Stream using WebDriver

  Scenario Outline: User log into the system

    Given I am on the login page
    When I login with <username> and <password>
    Then I should see a welcome message <message>

    Examples:
      | username | password             | message                        |
      | admin    | admin                | Welcome, admin |
     
