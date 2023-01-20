Feature: Taskset Request Processing

  Background: Common Test Setup
    Given TaskSet GRPC Port in system property "taskset-grpc-port"
    #Given Debug mode

  Scenario: Send a Task Set to location "x-location-001-x" and tenant "x-tenant-x"
    Given GRPC header "tenant-id" = "x-tenant-x"
    Given mock location "x-location-001-x"
    When Sample taskset is sent to service
    Then Taskset for location "x-location-001-x" and tenant "x-tenant-x" is available
    Then Taskset for location "x-location-001-x" and tenant "another" is not available

  Scenario: Subscribe task set of location "x-location-002-x" and tenant "tenant-a"
    # Context of Tenant A
    Given GRPC header "tenant-id" = "tenant-a"
    Given mock location "x-location-002-x"
    When Subscription "yzx" is created

    # Temporary switch to Tenant B
    Given GRPC header "tenant-id" = "tenant-b"
    Given mock location "x-location-002-y"
    When Subscription "abc" is created

    # Switch back to Tenant A
    Given GRPC header "tenant-id" = "tenant-a"
    Given mock location "x-location-002-x"
    When Sample taskset is sent to service
    Then Subscription "abc" did not receive update within 5000ms
    Then Subscription "yzx" received "created" notification within 5000ms

