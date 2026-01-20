package com.revature.fantastic4.e2e.steps;

import com.revature.fantastic4.e2e.fixtures.TestDataLoader;
import com.revature.fantastic4.e2e.pom.AssignedIssuesPage;
import com.revature.fantastic4.e2e.pom.DashboardPage;
import com.revature.fantastic4.e2e.pom.LoginPage;
import com.revature.fantastic4.e2e.pom.MyProjectsPage;
import com.revature.fantastic4.entity.*;
import com.revature.fantastic4.enums.IssueStatus;
import com.revature.fantastic4.enums.Priority;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.enums.Severity;
import com.revature.fantastic4.repository.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class DeveloperWorkflowSteps {

    @Autowired
    private WebDriver webDriver;

    @Autowired
    private TestDataLoader testDataLoader;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private IssueRepository issueRepository;

    private LoginPage loginPage;
    private AssignedIssuesPage assignedIssuesPage;
    private MyProjectsPage myProjectsPage;
    private Project testProject;
    private Issue testIssue;
    private User developerUser;
    private User adminUser;
    private User testerUser;

    @Before("@DeveloperWorkflow")
    public void setUp() {
        developerUser = testDataLoader.loadOrCreateUser("developer");
        adminUser = testDataLoader.loadOrCreateUser("admin");
        testerUser = testDataLoader.loadOrCreateUser("tester");
    }

    @After("@DeveloperWorkflow")
    public void tearDown() {
        if (testProject != null) {
            testDataLoader.cleanupProjectAndRelatedData(testProject.getId());
        }
    }

    @Given("I am logged in as a developer")
    public void iAmLoggedInAsADeveloper() {
        loginPage = new LoginPage(webDriver);
        loginPage.navigateToLogin();
        loginPage.login("developer", "password123");
        
        DashboardPage dashboardPage = new DashboardPage(webDriver);
        dashboardPage.waitForUrlToContain("/dashboard");
    }

    @Given("I have an assigned issue {string} with status {string}")
    public void iHaveAnAssignedIssueWithStatus(String issueTitle, String status) {
        adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        developerUser = userRepository.findByUsername("developer")
                .orElseThrow(() -> new RuntimeException("Developer user not found"));

        testerUser = userRepository.findByUsername("tester")
                .orElseThrow(() -> new RuntimeException("Tester user not found"));

 
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("Test project for developer");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setCreatedBy(adminUser);
        testProject.setCreatedAt(Instant.now());
        testProject.setUpdatedAt(Instant.now());
        testProject = projectRepository.save(testProject);

 
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(testProject);
        assignment.setUser(developerUser);
        assignment.setAssignedAt(Instant.now());
        projectAssignmentRepository.save(assignment);


        testIssue = new Issue();
        testIssue.setTitle(issueTitle);
        testIssue.setDescription("Test issue description");
        testIssue.setProject(testProject);
        testIssue.setCreatedBy(testerUser);
        testIssue.setAssignedTo(developerUser);
        testIssue.setSeverity(Severity.MEDIUM);
        testIssue.setPriority(Priority.HIGH);
        testIssue.setStatus(IssueStatus.valueOf(status));
        testIssue.setCreatedAt(Instant.now());
        testIssue.setUpdatedAt(Instant.now());
        testIssue = issueRepository.save(testIssue);
    }

    @When("I navigate to assigned issues page")
    public void iNavigateToAssignedIssuesPage() {
        assignedIssuesPage = new AssignedIssuesPage(webDriver);
        assignedIssuesPage.navigateToAssignedIssues();
    }

    @Then("I should see the issue {string} in assigned issues")
    public void iShouldSeeTheIssueInAssignedIssues(String issueTitle) {
        assertTrue(assignedIssuesPage.isIssueVisible(issueTitle),
                "Issue " + issueTitle + " should be visible in assigned issues");
    }

    @When("I click Start Work for issue {string}")
    public void iClickStartWorkForIssue(String issueTitle) {
        assignedIssuesPage.clickStartWorkForIssue(issueTitle);
    }

    @Then("the issue {string} status should be IN_PROGRESS")
    public void theIssueStatusShouldBeInProgress(String issueTitle) {
        String status = assignedIssuesPage.getIssueStatus(issueTitle);
        assertTrue(status.contains("IN_PROGRESS") || status.contains("IN PROGRESS"),
                "Issue status should be IN_PROGRESS, but was: " + status);
    }

    @When("I click Mark as Resolved for issue {string}")
    public void iClickMarkAsResolvedForIssue(String issueTitle) {
        assignedIssuesPage.clickMarkResolvedForIssue(issueTitle);
    }

    @Then("the issue {string} status should be RESOLVED")
    public void theIssueStatusShouldBeResolved(String issueTitle) {
        String status = assignedIssuesPage.getIssueStatus(issueTitle);
        assertTrue(status.contains("RESOLVED"),
                "Issue status should be RESOLVED, but was: " + status);
    }


}
