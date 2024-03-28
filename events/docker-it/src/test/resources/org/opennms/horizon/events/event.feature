@Event
Feature: Event Service Basic Functionality

  Background: Common Test Setup
    Given [Event] External GRPC Port in system property "application-external-grpc-port"
    Given [Event] Grpc TenantId "event-tenant-stream"
    Given A new tenant named "event-tenant-stream"
    Given [Event] Create Grpc Connection for Events

  Scenario:
    Given Initialize Trap Producer With Topic "traps" and BootstrapServer "kafka.bootstrap-servers"
    When Send Trap Data to Kafka Listener via Producer with TenantId "event-tenant-stream" and Location "1"
    Then verify there are 0 events
    Then Check If There are 1 Events with Location "Default"
