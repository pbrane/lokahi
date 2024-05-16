@Event
Feature: Event Service Basic Functionality

  Background: Common Test Setup
    Given [Event] External GRPC Port in system property "application-external-grpc-port"
    Given [Event] Grpc TenantId "event-tenant-stream-id"
    Given [Event] Create Grpc Connection for Events

  Scenario:
    Given Initialize Trap Producer With Topic "traps" and BootstrapServer "kafka.bootstrap-servers"
    When Send Trap Data to Kafka Listener via Producer with TenantId "event-tenant-stream-id" and Location "1"
    Then Check If There are any events
