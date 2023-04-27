@cloud
Feature: User can discover nodes on discovery page

  Scenario: Verify ICMP/SNMP discovery form validation
    Given Navigate to the "discovery" through the left panel
    Then Click on add discovery button
    Then Click on ICMP Active discovery radio button
    Then Click on 'Save Discovery' button
    Then Verify that the name is required
    Then Verify that the ip address is required
    Then Verify that tags field has "default" tag
    Then Verify that the location has minion location
    Then Verify that UDP port has "161" entered
    Then Click on 'Cancel' button

  Scenario: Active discovery ICMP/SNMP discovery test
    Given Navigate to the "discovery" through the left panel
    #Then Start SNMP docker image
    Then Click on add discovery button
    Then Click on ICMP Active discovery radio button
    Then Add an ICMP SNMP discovery name "Discovery Name"
    Then Verify that the location has minion location
    Then Verify that tags field has "default" tag
    Then Add additional tag "ActiveDiscovery"
    Then Click on 'NEW' button to add a new tag
    Then Add device IP address range "127.0.0.1 - 127.0.0.254"
    Then Verify that Community has "public" entered
    Then Verify that UDP port has "161" entered
    Then Click on 'Save Discovery' button
    Then Click on 'View Detected Nodes' button

  Scenario: Verify discovered nodes on Inventory page
    Given Navigate to the "inventory" through the left panel
    Then Find discovered node with ip "127.0.0.1"
    Then Remove discovered node with ip "127.0.0.1"
    When Confirm node delete pop up has ip "127.0.0.1"
    Then Click on 'Delete' to remove the discovered node
