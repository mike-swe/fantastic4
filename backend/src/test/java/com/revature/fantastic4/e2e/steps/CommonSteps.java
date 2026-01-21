package com.revature.fantastic4.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import com.revature.fantastic4.e2e.pom.DashboardPage;
import com.revature.fantastic4.e2e.pom.LoginPage;

import static org.junit.jupiter.api.Assertions.*;

public class CommonSteps {

    @Autowired
    private WebDriver webDriver;

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        loginPage = new LoginPage(webDriver);
        loginPage.navigateToLogin();
    }

    @Given("I am logged in as {string}")
    public void iAmLoggedInAs(String role) {
        loginPage = new LoginPage(webDriver);
        loginPage.navigateToLogin();
        
        String username = role.toLowerCase();
        String password = "password123";
        
        loginPage.login(username, password);
        
  
        dashboardPage = new DashboardPage(webDriver);
        dashboardPage.waitForUrlToContain("/dashboard");
        assertTrue(dashboardPage.isOnDashboard(), "Should be redirected to dashboard after login");
    }

    @When("I enter username {string} and password {string}")
    public void iEnterUsernameAndPassword(String username, String password) {
        loginPage = new LoginPage(webDriver);
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @When("I click the login button")
    public void iClickTheLoginButton() {
        loginPage.clickLogin();
    }

    @Then("I should be redirected to the dashboard")
    public void iShouldBeRedirectedToTheDashboard() {

        if (loginPage != null) {
            loginPage.waitForUrlToContain("/dashboard");
        } else {
   
            LoginPage tempLoginPage = new LoginPage(webDriver);
            tempLoginPage.waitForUrlToContain("/dashboard");
        }
    
        dashboardPage = new DashboardPage(webDriver);
     
        dashboardPage.getWelcomeMessage();
        
        String currentUrl = webDriver.getCurrentUrl();
        boolean isOnDashboard = dashboardPage.isOnDashboard();
        
        assertTrue(isOnDashboard, 
            "Should be on dashboard page. Current URL: " + currentUrl + 
            ", isOnDashboard() returned: " + isOnDashboard);
    }

    @Then("I should see an error message")
    public void iShouldSeeAnErrorMessage() {
        assertTrue(loginPage.isErrorMessageVisible(), "Error message should be visible");
    }

    @Then("I should remain on the login page")
    public void iShouldRemainOnTheLoginPage() {
        assertTrue(loginPage.isOnLoginPage(), "Should remain on login page");
    }

    @When("I navigate to my projects page")
    public void iNavigateToMyProjectsPage() {
        com.revature.fantastic4.e2e.pom.MyProjectsPage myProjectsPage = 
            new com.revature.fantastic4.e2e.pom.MyProjectsPage(webDriver);
        myProjectsPage.navigateToMyProjects();
    }

    @Then("I should see the project {string} in my projects")
    public void iShouldSeeTheProjectInMyProjects(String projectName) {
        com.revature.fantastic4.e2e.pom.MyProjectsPage myProjectsPage = 
            new com.revature.fantastic4.e2e.pom.MyProjectsPage(webDriver);
        assertTrue(myProjectsPage.isProjectVisible(projectName),
                "Project " + projectName + " should be visible in my projects");
    }
}
