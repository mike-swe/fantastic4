Feature: Login and Logout
  As a user
  I want to login and logout
  So that I can access the application securely

  Background:
    Given I am on the login page

  Scenario: Successful login with valid credentials
    When I login with username "admin" and password "password123"
    Then I should be successfully logged in
    And I should see the welcome message

  Scenario: Failed login with invalid credentials
    When I login with username "invalid" and password "wrongpassword"
    Then I should see an error message
    And I should remain on the login page

  Scenario: Login as admin
    When I login with username "admin" and password "password123"
    Then I should be redirected to the dashboard

  Scenario: Login as tester
    When I login with username "tester" and password "password123"
    Then I should be redirected to the dashboard

  Scenario: Login as developer
    When I login with username "developer" and password "password123"
    Then I should be redirected to the dashboard
