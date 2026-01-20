Feature: Developer Workflows
  As a developer
  I want to view and update assigned issues
  So that I can track my work progress

  @DeveloperWorkflow
  Scenario: Developer views assigned issues
    Given I am logged in as a developer
    And I have an assigned issue "Fix Login Bug" with status "OPEN"
    When I navigate to assigned issues page
    Then I should see the issue "Fix Login Bug" in assigned issues

  @DeveloperWorkflow
  Scenario: Developer updates issue status to IN_PROGRESS
    Given I am logged in as a developer
    And I have an assigned issue "Work in Progress Issue" with status "OPEN"
    When I navigate to assigned issues page
    And I click Start Work for issue "Work in Progress Issue"
    Then the issue "Work in Progress Issue" status should be IN_PROGRESS

  @DeveloperWorkflow
  Scenario: Developer updates issue status to RESOLVED
    Given I am logged in as a developer
    And I have an assigned issue "Resolved Issue" with status "IN_PROGRESS"
    When I navigate to assigned issues page
    And I click Mark as Resolved for issue "Resolved Issue"
    Then the issue "Resolved Issue" status should be RESOLVED

  @DeveloperWorkflow
  Scenario: Developer views assigned projects
    Given I am logged in as a developer
    And I have an assigned issue "Project View Issue" with status "OPEN"
    When I navigate to my projects page
    Then I should see the project "Test Project" in my projects
