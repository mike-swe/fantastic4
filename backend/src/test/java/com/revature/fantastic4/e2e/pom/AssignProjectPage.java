package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class AssignProjectPage extends BasePage {

    private static final By PROJECT_CARDS = By.className("project-card");
    private static final By PROJECT_NAME = By.className("project-name");
    private static final By USER_SELECT = By.className("user-select");
    private static final By ASSIGN_BUTTON = By.className("btn-assign");
    private static final By ASSIGNED_USERS_LIST = By.className("assigned-users-list");
    private static final By USER_BADGE = By.className("user-badge");
    private static final By USER_NAME = By.className("user-name");

    public AssignProjectPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToAssignProject() {
        navigateTo("http://localhost:4200/users");
        
        waitForUrlToContain("/users");

        try {
            waitForElementToBeVisible(PROJECT_CARDS);
        } catch (org.openqa.selenium.TimeoutException e) {

            String currentUrl = getCurrentUrl();
            if (!currentUrl.contains("/users")) {
                throw new RuntimeException("Failed to navigate to assign project page. Current URL: " + currentUrl + 
                        ". Ensure frontend is running on http://localhost:4200", e);
            }
        }
    }

    public List<WebElement> getProjectCards() {
        return driver.findElements(PROJECT_CARDS);
    }

    public void selectProject(String projectName) {
        List<WebElement> projectCards = getProjectCards();
        for (WebElement card : projectCards) {
            if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                return;
            }
        }
        throw new RuntimeException("Project not found: " + projectName);
    }

    public void selectUserForProject(String projectName, String username) {
        List<WebElement> projectCards = getProjectCards();
        for (WebElement card : projectCards) {
            if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                WebElement selectElement = card.findElement(USER_SELECT);
                Select select = new Select(selectElement);
                
                List<WebElement> allOptions = select.getOptions();
                
                String[] formats = {
                    username + " (TESTER)",
                    username.toLowerCase() + " (TESTER)",
                    username.toUpperCase() + " (TESTER)",
                    username + " (tester)",
                    username.toLowerCase() + " (tester)",
                    username
                };
                
                boolean selected = false;
                for (String format : formats) {
                    try {
                        select.selectByVisibleText(format);
                        selected = true;
                        break;
                    } catch (Exception e) {
                    }
                }
                
                if (!selected) {
            
                    List<WebElement> options = select.getOptions();
                    for (WebElement option : options) {
                        String optionText = option.getText();
                        String optionTextLower = optionText.toLowerCase();
                        String usernameLower = username.toLowerCase();
                        
                        // Check if option contains username and TESTER (case-insensitive)
                        if (optionTextLower.contains(usernameLower) && 
                            (optionTextLower.contains("tester") || optionTextLower.contains("test"))) {
                            select.selectByVisibleText(optionText);
                            selected = true;
                            break;
                        }
                    }
                }
                
                if (!selected) {
                    // Try selecting by value if we can find a matching option
                    for (WebElement option : allOptions) {
                        String optionText = option.getText().toLowerCase();
                        if (optionText.contains(username.toLowerCase()) && optionText.contains("tester")) {
                            String value = option.getAttribute("value");
                            if (value != null && !value.isEmpty()) {
                                try {
                                    select.selectByValue(value);
                                    selected = true;
                                    break;
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                
                if (!selected) {
                    StringBuilder errorMsg = new StringBuilder("Could not find user option for: " + username + 
                        " in project: " + projectName + ". Available options: ");
                    for (WebElement opt : allOptions) {
                        errorMsg.append("'").append(opt.getText()).append("', ");
                    }
                    throw new RuntimeException(errorMsg.toString());
                }
                return;
            }
        }
        throw new RuntimeException("Project not found: " + projectName);
    }

    public void selectDeveloperForProject(String projectName, String username) {
        List<WebElement> projectCards = getProjectCards();
        for (WebElement card : projectCards) {
            if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                WebElement selectElement = card.findElement(USER_SELECT);
                Select select = new Select(selectElement);
                
                List<WebElement> allOptions = select.getOptions();
                
               
                String[] formats = {
                    username + " (DEVELOPER)",
                    username.toLowerCase() + " (DEVELOPER)",
                    username.toUpperCase() + " (DEVELOPER)",
                    username + " (developer)",
                    username.toLowerCase() + " (developer)",
                    username
                };
                
                boolean selected = false;
                for (String format : formats) {
                    try {
                        select.selectByVisibleText(format);
                        selected = true;
                        break;
                    } catch (Exception e) {
                    }
                }
                
                if (!selected) {
       
                    List<WebElement> options = select.getOptions();
                    for (WebElement option : options) {
                        String optionText = option.getText();
                        String optionTextLower = optionText.toLowerCase();
                        String usernameLower = username.toLowerCase();
                        
                    
                        if (optionTextLower.contains(usernameLower) && 
                            (optionTextLower.contains("developer") || optionTextLower.contains("dev"))) {
                            select.selectByVisibleText(optionText);
                            selected = true;
                            break;
                        }
                    }
                }
                
                if (!selected) {
                    // Try selecting by value if we can find a matching option
                    for (WebElement option : allOptions) {
                        String optionText = option.getText().toLowerCase();
                        if (optionText.contains(username.toLowerCase()) && optionText.contains("developer")) {
                            String value = option.getAttribute("value");
                            if (value != null && !value.isEmpty()) {
                                try {
                                    select.selectByValue(value);
                                    selected = true;
                                    break;
                                } catch (Exception e) {
                          
                                }
                            }
                        }
                    }
                }
                
                if (!selected) {
                    StringBuilder errorMsg = new StringBuilder("Could not find developer option for: " + username + 
                        " in project: " + projectName + ". Available options: ");
                    for (WebElement opt : allOptions) {
                        errorMsg.append("'").append(opt.getText()).append("', ");
                    }
                    throw new RuntimeException(errorMsg.toString());
                }
                return;
            }
        }
        throw new RuntimeException("Project not found: " + projectName);
    }

    public void clickAssignButton(String projectName) {
        List<WebElement> projectCards = getProjectCards();
        for (WebElement card : projectCards) {
            if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                int initialUserCount = card.findElements(USER_BADGE).size();
                card.findElement(ASSIGN_BUTTON).click();
                wait.until(driver -> {
                    List<WebElement> updatedCards = getProjectCards();
                    for (WebElement updatedCard : updatedCards) {
                        if (updatedCard.findElement(PROJECT_NAME).getText().equals(projectName)) {
                            int newCount = updatedCard.findElements(USER_BADGE).size();
                            return newCount > initialUserCount;
                        }
                    }
                    return false;
                });
           
                wait.until(driver -> {
                    List<WebElement> updatedCards = getProjectCards();
                    for (WebElement updatedCard : updatedCards) {
                        if (updatedCard.findElement(PROJECT_NAME).getText().equals(projectName)) {
                            List<WebElement> badges = updatedCard.findElements(USER_BADGE);
                            // Wait for at least one badge to be visible and rendered
                            return !badges.isEmpty() && badges.stream().anyMatch(WebElement::isDisplayed);
                        }
                    }
                    return false;
                });
                return;
            }
        }
        throw new RuntimeException("Project not found: " + projectName);
    }

    public void assignUserToProject(String projectName, String username, String role) {
        if ("TESTER".equals(role)) {
            selectUserForProject(projectName, username);
        } else if ("DEVELOPER".equals(role)) {
            selectDeveloperForProject(projectName, username);
        }
        clickAssignButton(projectName);
    }

    public boolean isUserAssignedToProject(String projectName, String username) {
        // Wait for badges to be visible (they might still be rendering)
        try {
            wait.until(driver -> {
                List<WebElement> cards = driver.findElements(PROJECT_CARDS);
                for (WebElement card : cards) {
                    try {
                        if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                            List<WebElement> badges = card.findElements(USER_BADGE);
                            return !badges.isEmpty() || driver.findElements(USER_BADGE).size() > 0;
                        }
                    } catch (Exception e) {
                    
                    }
                }
                return false;
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            // Badges might not exist yet, continue to check anyway
        }
        

        List<WebElement> projectCards = getProjectCards();
        for (WebElement card : projectCards) {
            if (card.findElement(PROJECT_NAME).getText().equals(projectName)) {
                try {
                    List<WebElement> userBadges = card.findElements(USER_BADGE);
                    for (WebElement badge : userBadges) {
                        String badgeText = badge.findElement(USER_NAME).getText();
                        // Match username case-insensitively and allow partial matches (e.g., "tester1" matches "tester")
                        String badgeTextLower = badgeText.toLowerCase();
                        String usernameLower = username.toLowerCase();
                        if (badgeText.equals(username) || badgeTextLower.equals(usernameLower) || 
                            badgeTextLower.startsWith(usernameLower) || usernameLower.startsWith(badgeTextLower)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }
}