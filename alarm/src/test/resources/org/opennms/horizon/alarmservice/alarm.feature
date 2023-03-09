Feature: Alarm Service Basic Functionality

  Background: Configure base URLs
    Given Application base HTTP URL in system property "application.base-http-url"
    Given Application base gRPC URL in system property "application.base-grpc-url"
    Given Kafka bootstrap URL in system property "kafka.bootstrap-servers"
    Given Kafka event topic "events"
    Given Kafka alarm topic "alarms"

  Scenario: Verify when an event is received from Kafka, a new alarm is created
    Then Send event with UEI "uei.opennms.org/vendor/cisco/traps/SNMP_Link_Down" with tenant "tenantA"
    Then List alarms for tenant "tenantA", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 1 |
      | alarms[0].severity == MINOR |
    Then Verify alarm topic has 1 messages with tenant "tenantA"

  Scenario: Verify when an event is received from Kafka, with no matching alarm configuration, no new alarm is created
    Then Send event with UEI "uei.opennms.org/perspective/nodes/nodeLostService" with tenant "tenantB"
    Then Send GET request to application at path "/metrics/events_without_alarm_data_counter", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | measurements[0].value == 1.0 |
    Then Verify alarm topic has 0 messages with tenant "tenantB"

  Scenario: Verify when an event is received from Kafka with a different tenant id, you do not see the new alarm
    Then Send event with UEI "uei.opennms.org/vendor/cisco/traps/SNMP_Link_Down" with tenant "tenantA"
    Then List alarms for tenant "tenantA", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 1 |
    Then List alarms for tenant "tenant-other", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 0 |

  Scenario: Verify that alarms can be deleted
    Then Send event with UEI "uei.opennms.org/vendor/cisco/traps/SNMP_Link_Down" with tenant "tenantC"
    Then List alarms for tenant "tenantC", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 1 |
    Then Remember the first alarm from the last response
    Then Delete the alarm
    Then List alarms for tenant "tenantC", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 0 |

  Scenario: Verify that alarms can be cleared by other events
    Then Send event with UEI "uei.opennms.org/vendor/cisco/traps/SNMP_Link_Down" with tenant "tenantD"
    Then List alarms for tenant "tenantD", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 1 |
      | alarms[0].severity == MINOR |
    Then Send event with UEI "uei.opennms.org/vendor/cisco/traps/SNMP_Link_Up" with tenant "tenantD"
    Then List alarms for tenant "tenantD", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 2 |
      | alarms[0].severity == CLEARED |

  Scenario: Verify alarm can be acknowledged and unacknowledged
    Then Send event with UEI "uei.opennms.org/vendor/cisco/traps/SNMP_Link_Down" with tenant "tenantE"
    Then List alarms for tenant "tenantE", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 1 |
      | alarms[0].severity == MINOR |
    Then Remember the first alarm from the last response
    Then Acknowledge the alarm
    Then List alarms for tenant "tenantE", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 1 |
      | alarms[0].isAcknowledged == true |
      | alarms[0].ackUser == me |
      | alarms[0].ackTimeMs as long > 0 |
    Then Unacknowledge the alarm
    Then List alarms for tenant "tenantE", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 1 |
      | alarms[0].isAcknowledged == false |
      | alarms[0].ackTimeMs == 0 |

  Scenario: Verify alarm reduction for duplicate events
    Then Send event with UEI "uei.opennms.org/vendor/cisco/traps/SNMP_Link_Down" with tenant "tenantF"
    Then List alarms for tenant "tenantF", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 1 |
      | alarms[0].severity == MINOR |
    Then Verify alarm topic has 1 messages with tenant "tenantF"
    Then Send event with UEI "uei.opennms.org/vendor/cisco/traps/SNMP_Link_Down" with tenant "tenantF"
    Then List alarms for tenant "tenantF", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | alarms.size() == 1 |
      | alarms[0].counter == 2 |
      | alarms[0].severity == MINOR |
    Then Verify alarm topic has 2 messages with tenant "tenantF"
