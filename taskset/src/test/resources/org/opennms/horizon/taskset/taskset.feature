Feature: Taskset Request Processing

  Background: Common Test Setup
    Given TaskSet GRPC Port in system property "taskset-grpc-port"

  Scenario: Send a Task Set to location "x-location-001-x" and tenant "x-tenant-x"
    Given GRPC header "tenant-id" = "x-tenant-x"
    Given mock location "x-location-001-x"
    When Sample taskset is sent to service
    Then Taskset for location "x-location-001-x" and tenant "x-tenant-x" is available
    Then Taskset for location "x-location-001-x" and tenant "another" is not available

