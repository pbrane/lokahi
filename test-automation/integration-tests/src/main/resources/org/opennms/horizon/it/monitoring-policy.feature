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

    Given Policy rule name "<policy-rule>" and component type "NODE"
    Given Alert conditions data
#      | trigger_event id | trigger_event name  | count | overtime | overtime_unit | severity | clear_event |
      | <trigger-event-id> | <trigger-event-name> | 1     | 3        | MINUTE        | <severity>    |             |
    Then Create a monitoring policy with name "test-0002" and tag "<tag>"

    Given No Minion running with location "test-monitoring-policy-location".
    Given Location "test-monitoring-policy-location" does not exist.
    When Create location "test-monitoring-policy-location"
    Then Location "test-monitoring-policy-location" do exist.
    Then Request certificate for location "test-monitoring-policy-location".

    When Minion "test-monitoring-policy-system01" is started in location "test-monitoring-policy-location".
    When SNMP node "mp_snmp_node" is started in the network of minion "test-monitoring-policy-system01".
    Then Discover "MPDiscovery" for snmp node "mp_snmp_node", location "test-monitoring-policy-location" is created to discover by IP with policy tag "<tag>"
    Then Send a trap to Minion "test-monitoring-policy-system01" with oid "1.3.6.1.6.3.1.1.5.1"
      # coldStart
    Then Send a trap to Minion "test-monitoring-policy-system01" with oid "1.3.6.1.6.3.1.1.5.2"
      # warmStart
    Then Send a trap to Minion "test-monitoring-policy-system01" with oid "1.3.6.1.6.3.1.1.5.3"
      # linkDown
    Then Send a trap to Minion "test-monitoring-policy-system01" with oid "1.3.6.1.6.3.1.1.5.4"
      # linkUp
    Then Send a trap to Minion "test-monitoring-policy-system01" with oid "1.3.6.1.6.3.1.1.5.5"
      # authenticationFailure
    Then Send a trap to Minion "test-monitoring-policy-system01" with oid "1.3.6.1.6.3.1.1.5.6"
      # egpNeighborLoss

    Then The alert has the severity set to "<severity>"

    Given At least one Minion is running with location "test-monitoring-policy-location"
    Then Wait for at least one minion for the given location reported by inventory with timeout 180000ms
    Then Minion "test-monitoring-policy-system01" is stopped
    When Location "test-monitoring-policy-location" is removed
    Then Location "test-monitoring-policy-location" does not exist

    Examples:
      | tag | trigger-event-id | trigger-event-name | severity | policy-rule |
      | monitoring-policy-tag01 | 1 | SNMP Cold Start | MAJOR | policy-rule01 |

#   Test with severity MINOR failed and needs to revisit
#      | monitoring-policy-tag02 | 1 | snmp-trap-event | MINOR | policy-rule02 |

