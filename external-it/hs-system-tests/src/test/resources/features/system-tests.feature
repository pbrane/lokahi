Feature: Testing login to the cloud environment

  Background: Login to Cloud env
    Given Cloud url in environment variable "CLOUD_URL"
    Given Cloud username in environment variable "CLOUD_USERNAME"
    Given Cloud password in environment variable "CLOUD_PASSWORD"

  @cloud
  Scenario: Verify that we are able to login to the cluster
    Given Login to the web with provided login details
    Then Verify that we logged in successfully
