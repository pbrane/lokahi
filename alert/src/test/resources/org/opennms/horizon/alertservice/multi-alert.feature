@Alert
Feature: Multiple alerts can be generated by the same event
  A tenant can have two (or more) users. User A wants to get notified by pager
  duty when a node is down and considers it a Critical Alert. User B wants to
  get notified by email when a node is down and considers it a Major Alert.
  We need to be able to generate two different alerts - one for each monitoring
  policy.

  Background:
    Given Application base HTTP URL in system property "application.base-http-url"
    Given Application base gRPC URL in system property "application.base-grpc-url"
    Given Kafka bootstrap URL in system property "kafka.bootstrap-servers"
    Given Kafka event topic "events"
    Given Kafka alert topic "alerts"
    Given Kafka tag topic "tag-operation"
    Given A new tenant

  Scenario: Multiple monitoring policies can generate alerts from the same event
    Given A monitoring policy named "ma-policy-1"
    And The policy has a rule named "ma-p1-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 1     | 0        | MINUTE        | MAJOR    |                  |
    And The policy is created in the tenant
    And A monitoring policy named "ma-policy-2"
    And The policy has a rule named "ma-p2-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 1     | 0        | MINUTE        | MINOR    |                  |
    And The policy is created in the tenant
    Then An event is sent with UEI "uei.opennms.org/generic/traps/SNMP_Cold_Start" on node 10
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 2 |
    Then Count alerts for the tenant, assert response is 2
    Then Count alerts for the tenant, filtered by severity "MAJOR", assert response is 1
    Then Count alerts for the tenant, filtered by severity "MINOR", assert response is 1
    Then Verify alert topic has 2 messages for the tenant

  Scenario: A clear event from one monitoring policy will not clear an alert from another
    Given A monitoring policy named "ma-policy-1"
    And The policy has a rule named "ma-p1-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Link Down     | 1     | 0        | MINUTE        | MAJOR    |                  |
      | SNMP Link Up       | 1     | 0        | MINUTE        | CLEARED  | SNMP Link Down   |
    And The policy is created in the tenant
    And A monitoring policy named "ma-policy-2"
    And The policy has a rule named "ma-p2-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Link Down     | 1     | 0        | MINUTE        | MINOR    |                  |
    And The policy is created in the tenant
    When An event is sent with UEI "uei.opennms.org/generic/traps/SNMP_Link_Down" on node 10
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 2          |
      | alerts[0].counter == 1      |
      | alerts[0].severity == MAJOR |
      | alerts[1].counter == 1      |
      | alerts[1].severity == MINOR |
    When An event is sent with UEI "uei.opennms.org/generic/traps/SNMP_Link_Up" on node 10
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 2            |
      | alerts[0].counter == 2        |
      | alerts[0].severity == CLEARED |
      | alerts[1].counter == 1        |
      | alerts[1].severity == MINOR   |

  Scenario: Monitor Policy thresholds are independent of each other
    Given A monitoring policy named "ma-policy-1"
    And The policy has a rule named "ma-p1-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 1     | 0        | MINUTE        | MINOR    |                  |
    And The policy is created in the tenant
    And A monitoring policy named "ma-policy-2"
    And The policy has a rule named "ma-p2-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 3     | 10       | MINUTE        | MAJOR    |                  |
    And The policy is created in the tenant
    Then An event is sent with UEI "uei.opennms.org/generic/traps/SNMP_Cold_Start" on node 10
    Then An event is sent with UEI "uei.opennms.org/generic/traps/SNMP_Cold_Start" on node 10
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 1          |
      | alerts[0].counter == 2      |
      | alerts[0].severity == MINOR |
    Then An event is sent with UEI "uei.opennms.org/generic/traps/SNMP_Cold_Start" on node 10
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 2          |
      | alerts[0].counter == 3      |
      | alerts[0].severity == MINOR |
      | alerts[1].counter == 1      |
      | alerts[1].severity == MAJOR |
