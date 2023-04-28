@cloud
Feature: new

  Scenario: User sees empty 'Flows' chart before data was sent
    Given No netflow data was sent
    And sees "No applications data was found in last 24 hours.." subtitle in the 'Top 10 Applications' chart
    Then click on 'Flows' link
    And sees 'No Data' in the flows table

  Scenario: User sees 'Top 10 Applications' chart after flows data came to the instance
    Given send 200 packets of "netflow9" traffic to 9999 port
    And wait until the 'Top 10 Applications' chart will reflect the received data
    Then click on 'Flows' link
    And sees chart for netflow data

  Scenario: User has no exporters for Exporter if no device was added
    Given click on 'Flows' link
    Then click on 'Exporter' filter



