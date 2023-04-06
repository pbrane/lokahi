Feature: Email
  Background:
    Given gRPC setup

  Scenario: Can send email over SMTP
    When an email addressed to 'alerts@company.com' with subject 'New alert' and body 'Node down.' is received
    Then the email should be sent to the SMTP server
