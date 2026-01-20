package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class DashboardPage extends BasePage {

    private static final By WELCOME_MESSAGE = By.className("dashboard-title");
    private static final By PROJECT_CARDS = By.className("project-card");
    private static final By PROJECT_NAME = By.className("project-name");
    private static final By ISSUES_TABLE = By.className("issues-table");
    private static final By ISSUE_ROWS = By.cssSelector("tbody tr");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOnDashboard() {
        return getCurrentUrl().contains("/dashboard");
    }

    public String getWelcomeMessage() {
        waitForElementToBeVisible(WELCOME_MESSAGE);
        return getText(WELCOME_MESSAGE);
    }

    public List<WebElement> getProjectCards() {
        waitForElementToBeVisible(PROJECT_CARDS);
        return driver.findElements(PROJECT_CARDS);
    }

    public boolean isProjectVisible(String projectName) {
        List<WebElement> projectCards = getProjectCards();
        for (WebElement card : projectCards) {
            if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                return true;
            }
        }
        return false;
    }

    public int getProjectCount() {
        return getProjectCards().size();
    }

    public boolean isIssuesTableVisible() {
        return isElementVisible(ISSUES_TABLE);
    }

    public int getIssueCount() {
        if (isIssuesTableVisible()) {
            return driver.findElements(ISSUE_ROWS).size();
        }
        return 0;
    }
}
