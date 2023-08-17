@monitoring-policy
Feature: Monitoring Policy

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

  Scenario Outline: Create monitoring policies

    Given Policy rule name "<policy-rule>", component type "NODE" and event type "SNMP_TRAP"
    Given Alert conditions data
      | trigger event id   | trigger event name   | event type | count | overtime | overtime unit | severity   | clear event |
      | <trigger-event-id> | <trigger-event-name> | SNMP_TRAP  | 1     | 0        |               | <severity> |             |
    When The user saves the monitoring policy with name "test-monitoring-policy" and tag "<tag>"
    Then There is a monitoring policy with name "test-monitoring-policy" and tag "<tag>"

    Examples:
      | tag                     | trigger-event-id | trigger-event-name | severity | policy-rule   |
      | monitoring-policy-tag01 | 1                | SNMP Cold Start    | MINOR    | policy-rule01 |
      | monitoring-policy-tag01 | 2                | SNMP Warm Start    | MAJOR    | policy-rule02 |
      | monitoring-policy-tag01 | 4                | SNMP Link Down     | CRITICAL | policy-rule04 |

  Scenario: Send trap and verify alert severity

    Given No minion running with location "test-monitoring-policy-location"
    Given Location "test-monitoring-policy-location" does not exist
    When Create location "test-monitoring-policy-location"
    Then Location "test-monitoring-policy-location" should exist
    Then User can retrieve a certificate for location "test-monitoring-policy-location"

    When Minion "test-monitoring-policy-system01" is started at location "test-monitoring-policy-location"
    And SNMP node "mp_snmp_node" is started in the network of minion "test-monitoring-policy-system01"
    Then Discover "test-monitoring-policy-discovery" for snmp node "mp_snmp_node", location "test-monitoring-policy-location" is created to discover by IP with policy tag "monitoring-policy-tag01"

    When The snmp node sends a "SNMP Cold Start" to Minion
    Then An alert with "SNMP Cold Start" should be triggered with severity "MINOR"

    When The snmp node sends a "SNMP Warm Start" to Minion
    Then An alert with "SNMP Warm Start" should be triggered with severity "MAJOR"

    When The snmp node sends a "SNMP Link Down" to Minion
    Then An alert with "SNMP Link Down" should be triggered with severity "CRITICAL"

    Given At least one Minion is running with location "test-monitoring-policy-location"
    Then Minion "test-monitoring-policy-system01" is stopped
    When Location "test-monitoring-policy-location" is removed
    Then Location "test-monitoring-policy-location" does not exist