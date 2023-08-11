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

  Scenario Outline: Apply a custom monitoring policy to a node

    Given Policy rule name "<policy-rule>", component type "NODE" and event type "SNMP_TRAP"
    Given Alert conditions data
      | trigger event id   | trigger event name  | event type | count | overtime | overtime unit | severity   | clear event |
      | <trigger-event-id> | <trigger-event-name> | SNMP_TRAP | 1     | 3        | MINUTE        | <severity> |             |
    When The user saves the monitoring policy with name "test-monitoring-policy" and tag "<tag>"
    Then There is a monitoring policy with name "test-monitoring-policy" and tag "<tag>"

    Given No minion running with location "test-monitoring-policy-location"
    Given Location "test-monitoring-policy-location" does not exist
    When Create location "test-monitoring-policy-location"
    Then Location "test-monitoring-policy-location" should exist
    Then User can retrieve a certificate for location "test-monitoring-policy-location"

    When Minion "test-monitoring-policy-system01" is started at location "test-monitoring-policy-location"
    And SNMP node "mp_snmp_node" is started in the network of minion "test-monitoring-policy-system01"
    Then Discover "MPDiscovery" for snmp node "mp_snmp_node", location "test-monitoring-policy-location" is created to discover by IP with policy tag "<tag>"
    When The snmp node sends a coldStart SNMP trap to Minion
    And The snmp node sends a warmStart SNMP trap to Minion
    Then An alert should be triggered with severity "<severity>"

    Given At least one Minion is running with location "test-monitoring-policy-location"
    Then Minion "test-monitoring-policy-system01" is stopped
    When Location "test-monitoring-policy-location" is removed
    Then Location "test-monitoring-policy-location" does not exist

    Examples:
      | tag | trigger-event-id | trigger-event-name | severity | policy-rule |
      | monitoring-policy-tag-major | 1 | SNMP Event | MAJOR | policy-rule-major |
