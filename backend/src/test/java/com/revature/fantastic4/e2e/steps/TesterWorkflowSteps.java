package com.revature.fantastic4.e2e.steps;

import com.revature.fantastic4.e2e.fixtures.TestDataLoader;
import com.revature.fantastic4.e2e.pom.DashboardPage;
import com.revature.fantastic4.e2e.pom.IssuesPage;
import com.revature.fantastic4.e2e.pom.LoginPage;
import com.revature.fantastic4.e2e.pom.MyIssuesPage;
import com.revature.fantastic4.e2e.pom.MyProjectsPage;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.ProjectAssignment;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ProjectStatus;
import com.revature.fantastic4.repository.ProjectAssignmentRepository;
import com.revature.fantastic4.repository.ProjectRepository;
import com.revature.fantastic4.repository.UserRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class TesterWorkflowSteps {

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

    private LoginPage loginPage;
    private MyProjectsPage myProjectsPage;
    private IssuesPage issuesPage;
    private MyIssuesPage myIssuesPage;
    private Project testProject;
    private User testerUser;
    private User adminUser;

    @Before("@TesterWorkflow")
    public void setUp() {
        testerUser = testDataLoader.loadOrCreateUser("tester");
        adminUser = testDataLoader.loadOrCreateUser("admin");
    }

    @After("@TesterWorkflow")
    public void tearDown() {
        if (testProject != null) {
            testDataLoader.cleanupProjectAndRelatedData(testProject.getId());
        }
    }

    @Given("I am logged in as a tester")
    public void iAmLoggedInAsATester() {
        loginPage = new LoginPage(webDriver);
        loginPage.navigateToLogin();
        loginPage.login("tester", "password123");
        
        DashboardPage dashboardPage = new DashboardPage(webDriver);
        dashboardPage.waitForUrlToContain("/dashboard");
    }

    @Given("I am assigned to a project {string}")
    public void iAmAssignedToAProject(String projectName) {
        adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        testerUser = userRepository.findByUsername("tester")
                .orElseThrow(() -> new RuntimeException("Tester user not found"));


        testProject = new Project();
        testProject.setName(projectName);
        testProject.setDescription("Test project for tester");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setCreatedBy(adminUser);
        testProject.setCreatedAt(Instant.now());
        testProject.setUpdatedAt(Instant.now());
        testProject = projectRepository.save(testProject);

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(testProject);
        assignment.setUser(testerUser);
        assignment.setAssignedAt(Instant.now());
        projectAssignmentRepository.save(assignment);
    }



    @When("I navigate to the issues page")
    public void iNavigateToTheIssuesPage() {
        issuesPage = new IssuesPage(webDriver);
        issuesPage.navigateToIssues();
    }

    @When("I create a new issue with title {string}, description {string}, project {string}, severity {string}, and priority {string}")
    public void iCreateANewIssueWithDetails(String title, String description, String projectName, String severity, String priority) {
        int initialCount = issuesPage.getIssueCount();
        issuesPage.createIssue(title, description, projectName, severity, priority);
        

        issuesPage.waitForIssueCountToChange(initialCount);
    }

    @Then("I should see the issue {string} in the issues list")
    public void iShouldSeeTheIssueInTheIssuesList(String issueTitle) {
        assertTrue(issuesPage.isIssueVisible(issueTitle),
                "Issue " + issueTitle + " should be visible in the issues list");
    }

    @When("I navigate to my issues page")
    public void iNavigateToMyIssuesPage() {
        myIssuesPage = new MyIssuesPage(webDriver);
        myIssuesPage.navigateToMyIssues();
    }

    @Then("I should see the issue {string} in my issues")
    public void iShouldSeeTheIssueInMyIssues(String issueTitle) {
        assertTrue(myIssuesPage.isIssueVisible(issueTitle),
                "Issue " + issueTitle + " should be visible in my issues");
    }
}
