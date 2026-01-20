package com.revature.fantastic4.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import com.revature.fantastic4.e2e.pom.DashboardPage;
import com.revature.fantastic4.e2e.pom.LoginPage;

import static org.junit.jupiter.api.Assertions.*;

public class LoginSteps {

    @Autowired
    private WebDriver webDriver;

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @Given("I navigate to the login page")
    public void iNavigateToTheLoginPage() {
        loginPage = new LoginPage(webDriver);
        loginPage.navigateToLogin();
    }

    @When("I login with username {string} and password {string}")
    public void iLoginWithUsernameAndPassword(String username, String password) {
        if (loginPage == null) {
            loginPage = new LoginPage(webDriver);
        }
        loginPage.login(username, password);
    }

    @Then("I should be successfully logged in")
    public void iShouldBeSuccessfullyLoggedIn() {
        dashboardPage = new DashboardPage(webDriver);
        dashboardPage.waitForUrlToContain("/dashboard");
        assertTrue(dashboardPage.isOnDashboard(), "Should be redirected to dashboard");
    }

    @Then("I should see the welcome message")
    public void iShouldSeeTheWelcomeMessage() {
        dashboardPage = new DashboardPage(webDriver);
        String welcomeMessage = dashboardPage.getWelcomeMessage();
        assertNotNull(welcomeMessage, "Welcome message should be visible");
        assertFalse(welcomeMessage.isEmpty(), "Welcome message should not be empty");
    }

    @Then("I should see an error message {string}")
    public void iShouldSeeAnErrorMessage(String expectedMessage) {
        if (loginPage == null) {
            loginPage = new LoginPage(webDriver);
        }
        String actualMessage = loginPage.getErrorMessage();
        assertTrue(loginPage.isErrorMessageVisible(), "Error message should be visible");
        if (expectedMessage != null && !expectedMessage.isEmpty()) {
            assertTrue(actualMessage.contains(expectedMessage) || expectedMessage.contains(actualMessage),
                    "Error message should contain: " + expectedMessage);
        }
    }
}
