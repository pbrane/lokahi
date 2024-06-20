@Policy
Feature: Monitor policy gRPC Functionality

  Background: Configure base URLs
    Given Application base HTTP URL in system property "application.base-http-url"
    Given Application base gRPC URL in system property "application.base-grpc-url"
    Given Kafka bootstrap URL in system property "kafka.bootstrap-servers"
    Given Kafka event topic "events"
    Given Kafka alert topic "alerts"
    Given Kafka tag topic "tag-operation"
    Given Kafka monitoring policy topic "monitoring-policy"

  Scenario: The default monitoring policy should exist
    Given Tenant id "different-tenant"
    Then The default monitoring policy exist with name "default_policy" and all notification enabled
    Then Verify the default policy status is true
    Then Verify the default policy rule has name "default_rule" and component type "NODE"
    Then Verify the default monitoring policy has the following data
      | triggerEventName | severity |
      | SNMP Cold Start  | CRITICAL |
      | SNMP Warm Start  | MAJOR    |
      | Device Unreachable  | CRITICAL    |
      | Device Service Restored  | CLEARED    |

  Scenario: Verify alert can be created based on the default policy
    Given Tenant id "new-tenant"
    When An event is sent with UEI "uei.opennms.org/generic/traps/SNMP_Cold_Start" on node 10
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 1     |
      | alerts[0].counter == 1 |

  Scenario: Create a monitor policy with SNMP Trap event rule
    Given Tenant id "test-tenant"
    Given A monitoring policy named "test-policy" with tag "tag1", notifying by email
    Given The policy has a rule named "snmp-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 1     | 3        | MINUTE        | MAJOR    |                  |
    And The policy is created in the tenant
    Then Verify the new policy has been created
    Then List policy should contain 1
    Then Verify monitoring policy for tenant "test-tenant" is sent to Kafka

  Scenario: Create duplicate monitor policy name
    Given Tenant id "test-tenant"
    Given A monitoring policy named "test-policy" with tag "tag1", notifying by email
    Given The policy has a rule named "new-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 1     | 3        | MINUTE        | MAJOR    |                  |
    And The policy is created in the tenant
    Then Verify exception "StatusRuntimeException" thrown with message "INVALID_ARGUMENT: Duplicate monitoring policy with name test-policy"

  Scenario: Create duplicate monitor rule name
    Given Tenant id "test-tenant"
    Given A monitoring policy named "duplicate-rule-policy" with tag "tag1", notifying by email
    Given The policy has a simple rule named "rule1" with component type "NODE"
    Given The policy has a simple rule named "rule1" with component type "NODE"
    And The policy is created in the tenant
    Then Verify exception "StatusRuntimeException" thrown with message "INVALID_ARGUMENT: Duplicate monitoring rule with name rule1"


  Scenario: Create  monitor policy with disabled status
    Given Tenant id "test-tenant"
    Given A monitoring policy named "disabled-rule-policy" with tag "tag1" and with  status disabled
    Given The policy has a rule named "new-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 1     | 3        | MINUTE        | MAJOR    |                  |
    And The monitor policy is created in the tenant with disabled status
    Then verify that a new monitor policy is created with label "disabled-rule-policy" and with disabled status

  Scenario: Delete a monitor policy
    Given Tenant id "test-tenant1"
    Given A monitoring policy named "test-policy1" with tag "tag1", notifying by email
    Given The policy has a rule named "snmp-rule1" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 1     | 3        | MINUTE        | MAJOR    |                  |
    And The policy is created in the tenant
    When An event is sent with UEI "uei.opennms.org/generic/traps/SNMP_Cold_Start" on node 10
    Then Verify the new policy has been created
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 1     |
    Then Delete policy named "test-policy1"
    Then List policy should contain 0
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 0     |

  Scenario: Delete a monitor rule
    Given Tenant id "test-tenant2"
    Given A monitoring policy named "test-policy2" with tag "tag1", notifying by email
    Given The policy has a rule named "snmp-rule2" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 1     | 3        | MINUTE        | MAJOR    |                  |
    And The policy is created in the tenant
    When An event is sent with UEI "uei.opennms.org/generic/traps/SNMP_Cold_Start" on node 10
    Then Verify the new policy has been created
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 1     |
    Then Delete policy rule named "snmp-rule2" under policy named "test-policy2"
    Then List policy should contain 1
    Then List alerts for the tenant, until JSON response matches the following JSON path expressions
      | alerts.size() == 0     |

  Scenario: All Event Definitions are loaded by default
    Given Tenant id "any-tenant"
    Then Validate whether we have loaded all event definitions of size greater than or equal to 17267

  Scenario: Able to load all vendors
    Given Tenant id "any-tenant"
    Then Validate whether we can load vendors of size greater than or equal to 175

  @event-defs-by-vendor
  Scenario: Fetch event definitions for a given vendor
    Given Tenant id "any-tenant"
    Then Fetch event defs for vendor "generic" and verify size is greater than or equal to 7
    Then Fetch event defs for event type "INTERNAL" and verify size is greater than or equal to 1

  Scenario: Fetch minotor policies
    Given Tenant id "test-tenant"
    Given A monitoring policy named "search-test-policy" with tag "search-tag1", notifying by email
    Given The policy has a rule named "search-snmp-rule" with component type "NODE" and trap definitions
      | trigger_event_name | count | overtime | overtime_unit | severity | clear_event_name |
      | SNMP Cold Start    | 1     | 3        | MINUTE        | MAJOR    |                  |
    And The policy is created in the tenant
    Then Get list of moniotr policy  and verify that its size is greated then 1
    Then Get list of moniotr policy list sort by "name" and verify list is coming in sorting manner
    Then Get list of moniotr policy with page size 1  and verify list size is enqail to page size
