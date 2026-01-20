package com.revature.fantastic4.e2e.steps;

import com.revature.fantastic4.e2e.fixtures.TestDataLoader;
import com.revature.fantastic4.e2e.pom.AssignProjectPage;
import com.revature.fantastic4.e2e.pom.DashboardPage;
import com.revature.fantastic4.e2e.pom.LoginPage;
import com.revature.fantastic4.e2e.pom.ProjectsPage;
import com.revature.fantastic4.entity.Project;
import com.revature.fantastic4.entity.User;
import com.revature.fantastic4.enums.ProjectStatus;
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

public class AdminWorkflowSteps {

    @Autowired
    private WebDriver webDriver;

    @Autowired
    private TestDataLoader testDataLoader;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private LoginPage loginPage;
    private ProjectsPage projectsPage;
    private AssignProjectPage assignProjectPage;
    private DashboardPage dashboardPage;
    private Project testProject;
    private User adminUser;
    private User testerUser;
    private User developerUser;

    @Before("@AdminWorkflow")
    public void setUp() {
        
        adminUser = testDataLoader.loadOrCreateUser("admin");
        testerUser = testDataLoader.loadOrCreateUser("tester");
        developerUser = testDataLoader.loadOrCreateUser("developer");
    }

    @After("@AdminWorkflow")
    public void tearDown() {
    
        if (testProject != null) {
            testDataLoader.cleanupProjectAndRelatedData(testProject.getId());
        }
    }

    @Given("I am logged in as an admin")
    public void iAmLoggedInAsAnAdmin() {
        loginPage = new LoginPage(webDriver);
        loginPage.navigateToLogin();
        loginPage.login("admin", "password123");
        

        dashboardPage = new DashboardPage(webDriver);
        dashboardPage.waitForUrlToContain("/dashboard");
        assertTrue(dashboardPage.isOnDashboard(), "Should be on dashboard");
    }

    @When("I navigate to the projects page")
    public void iNavigateToTheProjectsPage() {
        projectsPage = new ProjectsPage(webDriver);
        projectsPage.navigateToProjects();
    }

    @When("I create a new project with name {string} and description {string}")
    public void iCreateANewProjectWithNameAndDescription(String name, String description) {
        int initialCount = projectsPage.getProjectCount();
        projectsPage.createProject(name, description);
        

        projectsPage.waitForProjectCountToChange(initialCount);
    }

    @Then("I should see the project {string} in the projects list")
    public void iShouldSeeTheProjectInTheProjectsList(String projectName) {
        assertTrue(projectsPage.isProjectVisible(projectName), 
                "Project " + projectName + " should be visible in the list");
    }

    @When("I navigate to the assign project page")
    public void iNavigateToTheAssignProjectPage() {
        assignProjectPage = new AssignProjectPage(webDriver);
        assignProjectPage.navigateToAssignProject();
    }

    @When("I assign user {string} with role {string} to project {string}")
    public void iAssignUserWithRoleToProject(String username, String role, String projectName) {
        assignProjectPage.assignUserToProject(projectName, username, role);
    }

    @Then("I should see user {string} assigned to project {string}")
    public void iShouldSeeUserAssignedToProject(String username, String projectName) {
        assertTrue(assignProjectPage.isUserAssignedToProject(projectName, username),
                "User " + username + " should be assigned to project " + projectName);
    }

    @When("I delete the project {string}")
    public void iDeleteTheProject(String projectName) {
      
        projectsPage.clickDeleteProject(projectName);
    }

    @Then("I should not see the project {string} in the projects list")
    public void iShouldNotSeeTheProjectInTheProjectsList(String projectName) {
        assertFalse(projectsPage.isProjectVisible(projectName),
                "Project " + projectName + " should not be visible");
    }

    @Given("a project {string} exists")
    public void aProjectExists(String projectName) {
        adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        testProject = new Project();
        testProject.setName(projectName);
        testProject.setDescription("Test project description");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setCreatedBy(adminUser);
        testProject.setCreatedAt(Instant.now());
        testProject.setUpdatedAt(Instant.now());
        testProject = projectRepository.save(testProject);
    }
}
