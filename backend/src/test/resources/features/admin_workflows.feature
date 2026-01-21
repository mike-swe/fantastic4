Feature: Admin Workflows
  As an admin
  I want to manage projects and assign users
  So that I can organize work effectively

  @AdminWorkflow
  Scenario: Admin creates a new project
    Given I am logged in as an admin
    When I navigate to the projects page
    And I create a new project with name "E2E Test Project" and description "This is a test project for E2E testing"
    Then I should see the project "E2E Test Project" in the projects list

  @AdminWorkflow
  Scenario: Admin assigns tester to project
    Given I am logged in as an admin
    And a project "Test Project for Assignment" exists
    When I navigate to the assign project page
    And I assign user "tester" with role "TESTER" to project "Test Project for Assignment"
    Then I should see user "tester" assigned to project "Test Project for Assignment"

  @AdminWorkflow
  Scenario: Admin assigns developer to project
    Given I am logged in as an admin
    And a project "Test Project for Developer" exists
    When I navigate to the assign project page
    And I assign user "developer" with role "DEVELOPER" to project "Test Project for Developer"
    Then I should see user "developer" assigned to project "Test Project for Developer"

  @AdminWorkflow
  Scenario: Admin deletes a project
    Given I am logged in as an admin
    And a project "Project to Delete" exists
    When I navigate to the projects page
    And I delete the project "Project to Delete"
    Then I should not see the project "Project to Delete" in the projects list
