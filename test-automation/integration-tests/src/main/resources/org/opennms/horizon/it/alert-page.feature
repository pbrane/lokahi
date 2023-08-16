@alert-page
Feature: Alert Page

  Background: Login to Keycloak
    Given Ingress base url in environment variable "INGRESS_BASE_URL"
    Given Keycloak server base url in environment variable "KEYCLOAK_BASE_URL"
    Given Keycloak realm in environment variable "KEYCLOAK_REALM"
    Given Keycloak username in environment variable "KEYCLOAK_USERNAME"
    Given Keycloak password in environment variable "KEYCLOAK_PASSWORD"
    Given Keycloak client-id in environment variable "KEYCLOAK_CLIENT_ID"
    Given Minion image name in environment variable "MINION_IMAGE_NAME"
    Given Minion ingress base url in environment variable "MINION_INGRESS_URL"
    Given Minion ingress port is in variable "MINION_INGRESS_PORT"
    Given Minion ingress TLS enabled flag is in variable "MINION_INGRESS_TLS"
    Given Minion ingress CA certificate file is in environment variable "MINION_INGRESS_CA"
    Given Minion ingress overridden authority is in variable "MINION_INGRESS_OVERRIDE_AUTHORITY"
    Then login to Keycloak with timeout 120000ms

  Scenario Outline: Generate SNMP traps and trigger alerts
    Given Policy rule name "<policy-rule>", component type "NODE" and event type "SNMP_TRAP"
    Given Alert conditions data
      | trigger event id   | trigger event name  | event type | count | overtime | overtime unit | severity   | clear event |
      | <trigger-event-id> | <trigger-event-name> | SNMP_TRAP | 1     | 0        | MINUTE        | <severity> |             |
    When The user saves the monitoring policy with name "<monitoring-policy>" and tag "<policy-tag>"
    Then There is a monitoring policy with name "<monitoring-policy>" and tag "<policy-tag>"

    Given No minion running with location "<alert-page-location>"
    Given Location "<alert-page-location>" does not exist
    When Create location "<alert-page-location>"
    Then Location "<alert-page-location>" should exist
    Then User can retrieve a certificate for location "<alert-page-location>"

    When Minion "<minion>" is started at location "<alert-page-location>"
    And SNMP node "<snmp-node>" is started in the network of minion "<minion>"
    Then Discover "<discovery>" for snmp node "<snmp-node>", location "<alert-page-location>" is created to discover by IP with policy tag "<policy-tag>"
    When The snmp node sends a coldStart SNMP trap to Minion
    And The snmp node sends a warmStart SNMP trap to Minion
    And The snmp node sends a linkDown SNMP trap to Minion
    And The snmp node sends a linkUp SNMP trap to Minion
    And The snmp node sends a authenticationFailure SNMP trap to Minion
    And The snmp node sends a egpNeighborLoss SNMP trap to Minion
    Then At least <alert-count> alerts have been triggered

    Given At least one Minion is running with location "<alert-page-location>"
    Then Minion "<minion>" is stopped
    When Location "<alert-page-location>" is removed
    Then Location "<alert-page-location>" does not exist

    Examples:
      | discovery        | alert-page-location     | monitoring-policy       | minion                    | snmp-node       | policy-tag        | trigger-event-id | trigger-event-name | severity | policy-rule | alert-count |
      | ap-discrovery-ma | alert-page-location-ma  | ap-monitoring-policy-ma | test-alert-page-system-ma | ap_snmp_node-ma | alert-page-tag-ma | 1 | SNMP Event Major | MAJOR | alert-page-policy-rule-ma | 0                 |
      | ap-discrovery-mi | alert-page-location2-mi | ap-monitoring-policy-mi | test-alert-page-system-mi | ap_snmp_node-mi | alert-page-tag-mi | 6 | SNMP Event Minor | MINOR | alert-page-policy-rule-mi | 0                 |
      | ap-discrovery-cr | alert-page-location3-cr | ap-monitoring-policy-cr | test-alert-page-system-cr | ap_snmp_node-cr | alert-page-tag-cr | 2 | SNMP Event Critical | CRITICAL | alert-page-policy-rule-cr | 0           |
      | ap-discrovery-in | alert-page-location3-in | ap-monitoring-policy-in | test-alert-page-system-in | ap_snmp_node-in | alert-page-tag-in | 3 | SNMP Event INDETERMINATE | INDETERMINATE | alert-page-policy-rule-in | 4 |
      | ap-discrovery-no | alert-page-location3-no | ap-monitoring-policy-no | test-alert-page-system-no | ap_snmp_node-no | alert-page-tag-no | 4 | SNMP Event NORMAL | NORMAL | alert-page-policy-rule-no | 0          |
      | ap-discrovery-wa | alert-page-location3-wa | ap-monitoring-policy-wa | test-alert-page-system-wa | ap_snmp_node-wa | alert-page-tag-wa | 2 | SNMP Event WARNING | WARNING | alert-page-policy-rule-wa | 3          |

  @skip
  Scenario: As a user I can view active alerts
    Given I am on the Alerts page
    When I view the Alert list
    Then I will see a list filtered by Alerts with an Active state ordered by "time"

  @skip
  Scenario: As a user I can sort alerts by time
    Given I am on the Alerts page
    When I view the Alert list
    And I sort the list by "time"
    Then I will see a list filtered by Alerts with an Active state ordered by "time"

  Scenario: As a user I can sort alerts by severity
    Given I am on the Alerts page
    When I view the Alert list
    And I sort the list by "severity"
    Then I will see a list filtered by Alerts with an Active state ordered by "severity"

  @skip
  Scenario: As a user I can sort alerts by node
    Given I am on the Alerts page
    When I view the Alert list
    And I sort the list by "nodeLabel"
    Then I will see a list filtered by Alerts with an Active state ordered by "nodeLabel"

  Scenario Outline: As a user I can filter alerts by a time window
    Given I am on the Alerts page
    When I view the Alert list
    And I pick a time window of "<time-window>"
    Then I will see a list filtered by Alerts that occurred in the time window

    Examples:
       | time-window   |
       | TODAY         |
       | LAST_24_HOURS |
       | SEVEN_DAYS    |

  @skip
  Scenario: As a user I can filter alerts by severity "CRITICAL"
    Given I am on the Alerts page
    When I view the Alert list
    And I filter the list by "severity"
    Then I will see a list filtered by Alerts with an Active state ordered by "nodeLabel"

  @skip
  Scenario: As a user I can filter alerts by node
    Given I am on the Alerts page
    When I view the Alert list
    And I filter the list by "nodeLabel"
    Then I will see a list filtered by Alerts with an Active state ordered by "nodeLabel"

  Scenario: As a user I can clear active filters
    Given I am on the Alerts page
    And I have a filter toggled for alerts with severity "CRITICAL"
    When I choose to clear filters
    Then alerts from all severities will show

  Scenario: As a user I can clear an active alert
    Given I am on the Alerts page
    And I have an ACTIVE alert
    When I choose to clear the alert
    Then the status of the Alert will change to "CLEARED"

  Scenario: As a user I can acknowledge an active alert
    Given I am on the Alerts page
    And I have an UNACKNOWLEDGED alert
    When I choose to acknowledge the alert
    Then the Alert is acknowledged
