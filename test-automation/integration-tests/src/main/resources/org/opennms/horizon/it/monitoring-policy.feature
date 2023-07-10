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

  Scenario: Apply a custom monitoring policy to a node

    Given Tenant id "test-mp-tenant"
    Given Monitor policy name "test-mp-policy" and memo "the test policy"
    Given Notify by email "true"
    Given Policy Rule name "snmp rule" and componentType "NODE"
    Given Monitor policy tag "monitoring_policy_uat_test"
    Given Trigger events data
#      | trigger_event   | count | overtime | overtime_unit | severity | clear_event |
      | SNMP_Cold_Start | 1     | 3        | MINUTE        | MINOR    |             |
    Then Create a new policy with given parameters

    Given No Minion running with location "MonitoringPolicy" MP
    Given Location "MonitoringPolicy" does not exist MP
    When Location "MonitoringPolicy" is created MP
    Then Location "MonitoringPolicy" do exist MP
    Then Request certificate for location "MonitoringPolicy" MP

    When Minion "test-minion-mp-sid" is started in location "MonitoringPolicy" MP
    When SNMP node "mp_snmp_node" is started in the network of minion "test-minion-mp-sid"
    Then Discover "MPDiscovery" for snmp node "mp_snmp_node", location "MonitoringPolicy" is created to discover by IP with policy tag "monitoring_policy_uat_test"
    Then Send a cold start trap to Minion "test-minion-mp-sid"
    Then The alert has the severity set to "MINOR"

    Given At least one Minion is running with location "MonitoringPolicy" MP
    Then Wait for at least one minion for the given location reported by inventory with timeout 180000ms MP
    Then Minion "test-minion-mp-sid" is stopped MP
    When Location "MonitoringPolicy" is removed MP
    Then Location "MonitoringPolicy" does not exist MP



