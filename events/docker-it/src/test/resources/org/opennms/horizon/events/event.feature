@Event
Feature: Event Service Basic Functionality

  Background: Common Test Setup
    Given [Event] External GRPC Port in system property "application-external-grpc-port"
    Given [Event] Grpc TenantId "event-tenant-stream-id"
    Given [Event] Create Grpc Connection for Events
    Given Kafka bootstrap URL in system property "kafka.bootstrap-servers"
    Given Kafka node topic "node"
    Given Kafka event topic "events"

  Scenario:
    Given Initialize Trap Producer With Topic "traps" and BootstrapServer "kafka.bootstrap-servers"
    When Send Trap Data to Kafka Listener via Producer with TenantId "event-tenant-stream-id" and Location "1"
    Then Check If There are any events

  Scenario: Receive external events and persist
    Given Initialize Trap Producer With Topic "events" and BootstrapServer "kafka.bootstrap-servers"
    Given [Event] data
      | tenant_id            | uei          | location_id | ip_address |
      | event-tenant-stream-id  | uei-external | 1           | 127.0.0.3  |
      | event-tenant-stream-id  | uei-external | 1           | 127.0.0.2  |
    When Send events message to Kafka topic "events" and tenant "event-tenant-stream-id"
    Then verify events persisted with uei "uei-external" with total rows 2

  Scenario: Receive internal events and persist
    Given Initialize Trap Producer With Topic "internal-event" and BootstrapServer "kafka.bootstrap-servers"
    Given [Event] data
      | tenant_id            | uei          | location_id | ip_address |
      | event-tenant-stream-id  | uei-internal | 1           | 127.0.0.3  |
      | event-tenant-stream-id  | uei-internal | 1           | 127.0.0.2  |
      | event-tenant-stream-id  | uei-internal | 1           | 127.0.0.1  |
    When Send events message to Kafka topic "internal-event" and tenant "event-tenant-stream-id"
    Then verify events persisted with uei "uei-internal" with total rows 3
