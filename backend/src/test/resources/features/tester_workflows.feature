Feature: Tester Workflows
  As a tester
  I want to create issues and view my work
  So that I can report defects effectively

  @TesterWorkflow
  Scenario: Tester views assigned projects
    Given I am logged in as a tester
    And I am assigned to a project "Tester Project"
    When I navigate to my projects page
    Then I should see the project "Tester Project" in my projects

  @TesterWorkflow
  Scenario: Tester creates a new issue
    Given I am logged in as a tester
    And I am assigned to a project "Issue Creation Project"
    When I navigate to the issues page
    And I create a new issue with title "Critical Bug Found", description "This is a critical bug that needs immediate attention", project "Issue Creation Project", severity "HIGH", and priority "CRITICAL"
    Then I should see the issue "Critical Bug Found" in the issues list

  @TesterWorkflow
  Scenario: Tester views created issues
    Given I am logged in as a tester
    And I am assigned to a project "My Issues Project"
    When I navigate to the issues page
    And I create a new issue with title "Test Issue", description "Test issue description", project "My Issues Project", severity "MEDIUM", and priority "HIGH"
    And I navigate to my issues page
    Then I should see the issue "Test Issue" in my issues
