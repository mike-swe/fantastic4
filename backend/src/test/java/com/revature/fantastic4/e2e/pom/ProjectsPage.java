package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ProjectsPage extends BasePage {

    private static final By NEW_PROJECT_BUTTON = By.className("btn-new-project");
    private static final By PROJECT_CARDS = By.className("project-card");
    private static final By PROJECT_NAME = By.className("project-name");
    private static final By EDIT_BUTTON = By.className("btn-edit");
    private static final By DELETE_BUTTON = By.className("btn-delete");
    private static final By MODAL_OVERLAY = By.className("modal-overlay");
    private static final By PROJECT_NAME_INPUT = By.id("project-name");
    private static final By PROJECT_DESCRIPTION_INPUT = By.id("project-description");
    private static final By CREATE_BUTTON = By.className("btn-create");
    private static final By MODAL_CLOSE_BUTTON = By.className("modal-close-btn");

    public ProjectsPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToProjects() {
        navigateTo("http://localhost:4200/projects");
        
       
        waitForUrlToContain("/projects");
        
        try {
            waitForElementToBeVisible(PROJECT_CARDS);
        } catch (org.openqa.selenium.TimeoutException e) {
    
            String currentUrl = getCurrentUrl();
            if (!currentUrl.contains("/projects")) {
                throw new RuntimeException("Failed to navigate to projects page. Current URL: " + currentUrl + 
                        ". Ensure frontend is running on http://localhost:4200", e);
            }
        }
    }

    public void clickNewProject() {
        waitForElementToBeClickable(NEW_PROJECT_BUTTON);
        clickElement(NEW_PROJECT_BUTTON);
        waitForElementToBeVisible(MODAL_OVERLAY);
    }

    public void enterProjectName(String name) {
        waitForElementToBeVisible(PROJECT_NAME_INPUT);
        enterText(PROJECT_NAME_INPUT, name);
    }

    public void enterProjectDescription(String description) {
        waitForElementToBeVisible(PROJECT_DESCRIPTION_INPUT);
        enterText(PROJECT_DESCRIPTION_INPUT, description);
    }

    public void clickCreateProject() {
        clickElement(CREATE_BUTTON);

        waitForElementToDisappear(MODAL_OVERLAY);
    }

    public void createProject(String name, String description) {
        clickNewProject();
        enterProjectName(name);
        if (description != null && !description.isEmpty()) {
            enterProjectDescription(description);
        }
        clickCreateProject();
    }

    public boolean isProjectVisible(String projectName) {
        List<WebElement> projectCards = getProjectCards();
        if (projectCards.isEmpty()) {
            return false;
        }
        
        for (WebElement card : projectCards) {
            try {
                String cardProjectName = card.findElement(PROJECT_NAME).getText();
                if (cardProjectName.equals(projectName)) {
                    return true;
                }
            } catch (Exception e) {
            
            }
        }
        
        return false;
    }

    public List<WebElement> getProjectCards() {
        return driver.findElements(PROJECT_CARDS);
    }

    public void clickEditProject(String projectName) {
        List<WebElement> projectCards = getProjectCards();
        for (WebElement card : projectCards) {
            if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                card.findElement(EDIT_BUTTON).click();
                waitForElementToBeVisible(MODAL_OVERLAY);
                return;
            }
        }
    }

    public void clickDeleteProject(String projectName) {
        List<WebElement> projectCards = getProjectCards();
        int initialCount = projectCards.size();
        for (WebElement card : projectCards) {
            if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                card.findElement(DELETE_BUTTON).click();
                
          
                WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                try {
                    Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
                    alert.accept();
                } catch (Exception e) {
     
                }
                
                waitForElementCountToChange(PROJECT_CARDS, initialCount);
                return;
            }
        }
    }

    public void closeModal() {
        if (isElementVisible(MODAL_CLOSE_BUTTON)) {
            clickElement(MODAL_CLOSE_BUTTON);
        }
    }

    public int getProjectCount() {
        return getProjectCards().size();
    }

    public void waitForProjectCountToChange(int initialCount) {
        waitForElementCountToChange(PROJECT_CARDS, initialCount);
    }
}
