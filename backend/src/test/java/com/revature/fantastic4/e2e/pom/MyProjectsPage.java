package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class MyProjectsPage extends BasePage {

    private static final By PROJECT_CARDS = By.className("project-card");
    private static final By PROJECT_NAME = By.className("project-name");

    public MyProjectsPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToMyProjects() {
        navigateTo("http://localhost:4200/my-projects");
        waitForElementToBeVisible(PROJECT_CARDS);
    }

    public List<WebElement> getProjectCards() {
        return driver.findElements(PROJECT_CARDS);
    }

    public boolean isProjectVisible(String projectName) {
        List<WebElement> projectCards = getProjectCards();
        for (WebElement card : projectCards) {
            try {
                if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public int getProjectCount() {
        return getProjectCards().size();
    }
}
