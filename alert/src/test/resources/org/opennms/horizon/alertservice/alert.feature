# NOTE: using retries after sending messages to Kafka to avoid failures due to race condition on testing
Feature: Alert Service Basic Functionality

  Background: Configure base URLs
    Given Application Base URL in system property "application.base-url"
    Given Kafka Bootstrap URL in system property "kafka.bootstrap-servers"
    Given Kafka topics "events" "events"
    Given Kafka topics "alerts" "alerts"

  Scenario: Verify when an event is received from Kafka, a new Alert is created
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.010" with tenant "opennms-prime"
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 1 |
    Then Verify topic "alerts" has 1 messages with tenant "opennms-prime"

  Scenario: Verify when an event is received from Kafka, a new Alert is created
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.020" with tenant "opennms-prime"
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 2 |
    Then Verify topic "alerts" has 2 messages with tenant "opennms-prime"

  Scenario: Verify when an event is received from Kafka with a different tenant id, you do not see the new alert
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.020" with tenant "other-tenant"
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 2 |
    Then Verify topic "alerts" has 2 messages with tenant "opennms-prime"
    Then Verify topic "alerts" has 1 messages with tenant "other-tenant"

  Scenario: Verify alert can be deleted
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.030" with tenant "opennms-prime"
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 3 |
    Then Remember alert id
    Then Send DELETE request to application at path "/alerts/delete"
    Then Verify the HTTP response code is 200
    Then Send GET request to application at path "/alerts/list"
    Then DEBUG dump the response body
    Then parse the JSON response
    Then Verify JSON path expressions match
      | totalCount == 2 |

  Scenario: Verify alert can be cleared
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.040" with tenant "opennms-prime"
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 3 |
    Then Send POST request to clear alert at path "/alerts/clear"
    Then Verify the HTTP response code is 200
    Then Send GET request to application at path "/alerts/list"
    Then Verify alert was cleared
    Then Send POST request to clear alert at path "/alerts/unclear"
    Then Verify the HTTP response code is 200
    Then Send GET request to application at path "/alerts/list"
    Then Verify alert was uncleared

  Scenario: Verify alert can be acknowledged and unacknowledged
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.050" with tenant "opennms-prime"
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 4 |
    Then Send POST request to acknowledge alert at path "/alerts/ack"
    Then Verify the HTTP response code is 200
    Then Send GET request to application at path "/alerts/list"
    Then Verify alert was acknowledged
    Then Send POST request to unacknowledge alert at path "/alerts/unAck"
    Then Verify the HTTP response code is 200
    Then Send GET request to application at path "/alerts/list"
    Then Verify alert was unacknowledged

  Scenario: Verify alert severity can be set and escalated
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.060" with tenant "opennms-prime"
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 5 |
    Then Send POST request to set alert severity at path "/alerts/severity"
    Then Verify the HTTP response code is 200
    Then Send GET request to application at path "/alerts/list"
#    Then Verify alert was acknowledged
    Then Send POST request to escalate alert severity at path "/alerts/escalate"
    Then Verify the HTTP response code is 200
    Then Send GET request to application at path "/alerts/list"
    Then Verify alert severity was escalated

  Scenario: Verify alert reduction for duplicate events
    # Generate an alert
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.070" with tenant "opennms-prime"
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 6        |
      | alerts[0].counter == 1 |

    # Generate a duplicate
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.070" with tenant "opennms-prime"
    Then Verify the HTTP response code is 200
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 6        |
      | alerts[5].counter == 2 |

  Scenario: Verify alert memo can be updated and removed
    Then Send Event message to Kafka at topic "events" with alert reduction key "alert.reduction-key.080" with tenant "opennms-prime"
    Then send GET request to application at path "/alerts/list", with timeout 5000ms, until JSON response matches the following JSON path expressions
      | totalCount == 7 |
    Then Send PUT request to add memo at path "/alerts/memo"
    Then Verify the HTTP response code is 200
    Then Send GET request to application at path "/alerts/list"
    Then DEBUG dump the response body
    Then Remember alert id
#    Then parse the JSON response
    Then Send DELETE request to remove memo at path "/alerts/removeMemo"
    Then Verify the HTTP response code is 200

