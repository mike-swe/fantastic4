package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class IssuesPage extends BasePage {

    private static final By NEW_ISSUE_BUTTON = By.className("btn-new-issue");
    private static final By ISSUE_CARDS = By.className("issue-card");
    private static final By ISSUE_TITLE = By.className("issue-title");
    private static final By MODAL_OVERLAY = By.className("modal-overlay");
    private static final By ISSUE_TITLE_INPUT = By.id("issue-title");
    private static final By ISSUE_DESCRIPTION_INPUT = By.id("issue-description");
    private static final By ISSUE_PROJECT_SELECT = By.id("issue-project");
    private static final By ISSUE_SEVERITY_SELECT = By.id("issue-severity");
    private static final By ISSUE_PRIORITY_SELECT = By.id("issue-priority");
    private static final By CREATE_ISSUE_BUTTON = By.className("btn-create");

    public IssuesPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToIssues() {
        navigateTo("http://localhost:4200/issues");
        
        waitForUrlToContain("/issues");
        
        try {
            List<WebElement> loadingSpinners = driver.findElements(By.className("loading-spinner"));
            if (!loadingSpinners.isEmpty()) {
                try {
                    waitForElementToDisappear(By.className("loading-spinner"));
                } catch (Exception e) {
                }
            }
            
            // Wait for either issue cards OR empty state message to appear
            WebDriverWait apiWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            apiWait.until(driver -> {
                List<WebElement> cards = driver.findElements(ISSUE_CARDS);
                List<WebElement> emptyState = driver.findElements(By.className("empty-state"));
                boolean hasCards = !cards.isEmpty();
                boolean hasEmptyState = !emptyState.isEmpty();
                // Also check if the issues-grid div exists (indicates page structure is ready)
                List<WebElement> issuesGrid = driver.findElements(By.className("issues-grid"));
                boolean hasGrid = !issuesGrid.isEmpty();

                return hasCards || hasEmptyState || hasGrid;
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            String currentUrl = getCurrentUrl();
            if (!currentUrl.contains("/issues")) {
                throw new RuntimeException("Failed to navigate to issues page. Current URL: " + currentUrl + 
                        ". Ensure frontend is running on http://localhost:4200", e);
            }
        }
    }

    public void clickNewIssue() {
        if (isElementVisible(NEW_ISSUE_BUTTON)) {
            WebElement button = driver.findElement(NEW_ISSUE_BUTTON);
            // Use JavaScript click to ensure Angular handler fires
            clickElementWithJavaScript(button);
            WebDriverWait modalWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            modalWait.until(driver -> {
                List<WebElement> overlays = driver.findElements(MODAL_OVERLAY);
                return !overlays.isEmpty() && overlays.get(0).isDisplayed();
            });
        }
    }

    public void enterIssueTitle(String title) {
        waitForElementToBeVisible(ISSUE_TITLE_INPUT);
        WebElement titleInput = driver.findElement(ISSUE_TITLE_INPUT);
        titleInput.clear();
        titleInput.sendKeys(title);
        getJavascriptExecutor().executeScript(
            "arguments[0].value = arguments[1]; " +
            "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            titleInput, title);
    }

    public void enterIssueDescription(String description) {
        waitForElementToBeVisible(ISSUE_DESCRIPTION_INPUT);
        WebElement descInput = driver.findElement(ISSUE_DESCRIPTION_INPUT);
        descInput.clear();
        descInput.sendKeys(description);
        getJavascriptExecutor().executeScript(
            "arguments[0].value = arguments[1]; " +
            "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            descInput, description);
    }

    public void selectProject(String projectName) {
        waitForElementToBeVisible(ISSUE_PROJECT_SELECT);
        WebElement selectElement = driver.findElement(ISSUE_PROJECT_SELECT);
        Select select = new Select(selectElement);
        
        WebDriverWait optionsWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        optionsWait.until(driver -> {
            List<WebElement> options = select.getOptions();
            boolean hasProjects = options.size() > 1;
            return hasProjects;
        });
        

        List<WebElement> options = select.getOptions();
 
        // Find the option by visible text (project name) and select by its value (project ID)
        // The frontend uses [value]="project.id" so we need to select by value, not visible text
        boolean found = false;
        for (WebElement option : options) {
            String optionText = option.getText().trim();
            String optionValue = option.getAttribute("value");
            
            if (optionValue == null || optionValue.isEmpty()) {
                continue;
            }
            
            // Match by project name (exact or partial, case-insensitive)
            String optionTextLower = optionText.toLowerCase();
            String projectNameLower = projectName.toLowerCase();
            if (optionTextLower.equals(projectNameLower) || 
                optionTextLower.contains(projectNameLower) || 
                projectNameLower.contains(optionTextLower)) {
                select.selectByValue(optionValue);

                getJavascriptExecutor().executeScript(
                    "arguments[0].value = arguments[1]; " +
                    "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    selectElement, optionValue);
                
                found = true;
                break;
            }
        }
        
        if (!found) {
        throw new RuntimeException("Project not found in dropdown: " + projectName);
        }
        
        getJavascriptExecutor().executeScript(
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", selectElement);
        

        wait.until(driver -> {
            WebElement currentSelect = driver.findElement(ISSUE_PROJECT_SELECT);
            String currentValue = currentSelect.getAttribute("value");
            return currentValue != null && !currentValue.isEmpty();
        });
    }

    public void selectSeverity(String severity) {
        waitForElementToBeVisible(ISSUE_SEVERITY_SELECT);
        WebElement selectElement = driver.findElement(ISSUE_SEVERITY_SELECT);
        Select select = new Select(selectElement);
        select.selectByVisibleText(severity);
        

        wait.until(driver -> {
            WebElement currentSelect = driver.findElement(ISSUE_SEVERITY_SELECT);
            String currentValue = currentSelect.getAttribute("value");
            return currentValue != null && !currentValue.isEmpty();
        });
    }

    public void selectPriority(String priority) {
        waitForElementToBeVisible(ISSUE_PRIORITY_SELECT);
        WebElement selectElement = driver.findElement(ISSUE_PRIORITY_SELECT);
        Select select = new Select(selectElement);
        select.selectByVisibleText(priority);
        

        wait.until(driver -> {
            WebElement currentSelect = driver.findElement(ISSUE_PRIORITY_SELECT);
            String currentValue = currentSelect.getAttribute("value");
            return currentValue != null && !currentValue.isEmpty();
        });
    }

    public void clickCreateIssue() {
            // Re-trigger Angular events and wait for form values to be processed.
            // This ensures Angular's change detection has updated the form model
            // before checking if the Create button is enabled, preventing timing issues
         // where the button remains disabled even though the DOM has values.
        try {
            WebElement titleInput = driver.findElement(ISSUE_TITLE_INPUT);
            WebElement descInput = driver.findElement(ISSUE_DESCRIPTION_INPUT);
            WebElement projectSelect = driver.findElement(ISSUE_PROJECT_SELECT);
            
            getJavascriptExecutor().executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
                "arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));",
                titleInput);
            getJavascriptExecutor().executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
                "arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));",
                descInput);
            getJavascriptExecutor().executeScript(
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                projectSelect);
            
            wait.until(driver -> {
                String titleValue = titleInput.getAttribute("value");
                String descValue = descInput.getAttribute("value");
                String projectValue = projectSelect.getAttribute("value");
                return titleValue != null && !titleValue.trim().isEmpty() &&
                       descValue != null && !descValue.trim().isEmpty() &&
                       projectValue != null && !projectValue.isEmpty();
            });
        } catch (Exception e) {
        }
        
       
        WebElement createButton = wait.until(driver -> {
            List<WebElement> buttons = driver.findElements(CREATE_ISSUE_BUTTON);
            return buttons.isEmpty() ? null : buttons.get(0);
        });
        
        WebDriverWait buttonWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        buttonWait.until(driver -> {
            try {
                String disabled = createButton.getAttribute("disabled");
                boolean isEnabled = createButton.isEnabled();
                boolean buttonEnabled = (disabled == null || disabled.equals("false")) && isEnabled;
                return buttonEnabled;
            } catch (Exception e) {
                return false;
            }
        });
        
        clickElementWithJavaScript(createButton);

        WebDriverWait modalWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        modalWait.until(driver -> {
            List<WebElement> overlays = driver.findElements(MODAL_OVERLAY);
            return overlays.isEmpty() || !overlays.get(0).isDisplayed();
        });
    }

    public void createIssue(String title, String description, String projectName, String severity, String priority) {
        clickNewIssue();
        
        enterIssueTitle(title);
        
        enterIssueDescription(description);
        
        selectProject(projectName);
        
        selectSeverity(severity);
        
        selectPriority(priority);
        
        clickCreateIssue();
    }

    public boolean isIssueVisible(String issueTitle) {
        List<WebElement> issueCards = getIssueCards();
        if (issueCards.isEmpty()) {
            return false;
        }
        
        for (WebElement card : issueCards) {
            try {
                String cardIssueTitle = card.findElement(ISSUE_TITLE).getText();
                if (cardIssueTitle.equals(issueTitle)) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        
        return false;
    }

    public List<WebElement> getIssueCards() {
        return driver.findElements(ISSUE_CARDS);
    }

    public int getIssueCount() {
        return getIssueCards().size();
    }

    public void waitForIssueCountToChange(int initialCount) {
        waitForElementCountToChange(ISSUE_CARDS, initialCount);
    }
}
