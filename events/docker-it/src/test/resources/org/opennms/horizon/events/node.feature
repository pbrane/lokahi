@Node
Feature: Node operation feature

  Background: Configure base URLs
    Given [Event] External GRPC Port in system property "application-external-grpc-port"
    Given [Event] Grpc TenantId "event-tenant-stream-id"
    Given [Event] Create Grpc Connection for Events
    Given Kafka bootstrap URL in system property "kafka.bootstrap-servers"
    Given Kafka node topic "node"
    Given Kafka event topic "events"

  Scenario: Insert node when receive new message
    Given Tenant id "tenant-1"
    Given [Node] operation data
      | id | tenant_id | label  |
      | 1  | tenant-1  | first  |
      | 2  | tenant-2  | second |
    And Sent node message to Kafka topic
    Then Verify node topic has 2 message for node operation "UPDATE_NODE"


  Scenario: Delete node when receive new message
    Given Tenant id "tenant-4"
    Given [Node] operation data
      | id   | tenant_id | label |
      | 155  | event-tenant-stream-id  | four  |
    And Sent node message to Kafka topic
    Given Initialize Trap Producer With Topic "events" and BootstrapServer "kafka.bootstrap-servers"
    Given [Event] data
      | tenant_id            | uei          | location_id | ip_address |  node_id |
      | event-tenant-stream-id  | uei-external | 1           | 127.0.0.3  |  155  |
      | event-tenant-stream-id  | uei-external | 1           | 127.0.0.1  |  123  |
    When Send events message to Kafka topic "events" and tenant "event-tenant-stream-id"
    Then Sent node for deletion using Kafka topic
    Then Verify node topic has 1 message for node operation "REMOVE_NODE"
    Then Verify all events are remove once node with id 155 is deleted
