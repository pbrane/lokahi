@Event
Feature: Event Service Basic Functionality

  Background: Common Test Setup
    Given [Event] External GRPC Port in system property "application-external-grpc-port"
    Given [Event] Grpc TenantId "event-tenant-stream"
    Given [Event] Create Grpc Connection for Events

    #Then [Common] Add a device with IP address = "192.168.1.1" with label "test-label" and location "Default"

    Scenario:
      Given Initialize Trap Producer With Topic "traps" and BootstrapServer "kafka.bootstrap-servers"
      #Given Create "Default" Location
      When Send Trap Data to Kafka Listener via Producer with TenantId "event-tenant-stream" and Location "Default"
      Then verify there are 0 events
      Then Check If There are 1 Events with Location "Default"
