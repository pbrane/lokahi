@flows
Feature: NetFlows basic function

  @ignore
  Scenario: User sees empty 'Flows' chart before data was sent
    Given No netflow data was sent
    And sees "No applications data was found in last 24 hours" subtitle in the Top 10 Applications chart
    Then click on 'Flows' link
    And sees 'No Data' in the flows table

  @ignore
  Scenario: User sees flows data sent to the instance
    # Missing discoverable image needs to be merged into repo, then the rest
    # of these tests can be merged with the other test code using TestContainers

    Given Start snmp node "flowNode"
#    When Discovery "SingleDiscovery" for node "flowNode" is created to discover by IP
#    Then Status of "flowNode" should be "UP"

#    Then Navigate to the "" through the left panel
#    And wait until the 'Top 10 Applications' chart will reflect the received data
#    Then click on 'Flows' link
#    And sees chart for netflow data
#    Then click on 'Exporter' filter
#    And check if exporter "flowNode" visible in the dropdown

