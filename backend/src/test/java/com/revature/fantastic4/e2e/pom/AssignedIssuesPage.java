package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AssignedIssuesPage extends BasePage {

    private static final By ISSUE_CARDS = By.className("issue-card");
    private static final By ISSUE_TITLE = By.className("issue-title");
    private static final By START_WORK_BUTTON = By.xpath(".//button[contains(text(), 'Start Work')]");
    private static final By MARK_RESOLVED_BUTTON = By.xpath(".//button[contains(text(), 'Mark as Resolved')]");
    private static final By STATUS_TAG = By.className("tag");

    public AssignedIssuesPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToAssignedIssues() {
        navigateTo("http://localhost:4200/assigned-issues");
        
        waitForUrlToContain("/assigned-issues");
        
     
        try {
            waitForElementToBeVisible(ISSUE_CARDS);
        } catch (org.openqa.selenium.TimeoutException e) {
     
            String currentUrl = getCurrentUrl();
            if (!currentUrl.contains("/assigned-issues")) {
                throw new RuntimeException("Failed to navigate to assigned issues page. Current URL: " + currentUrl + 
                        ". Ensure frontend is running on http://localhost:4200", e);
            }
        }
    }

    public List<WebElement> getIssueCards() {
        return driver.findElements(ISSUE_CARDS);
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

    public void clickStartWorkForIssue(String issueTitle) {
        List<WebElement> issueCards = getIssueCards();
        for (WebElement card : issueCards) {
            if (card.findElement(ISSUE_TITLE).getText().equals(issueTitle)) {
         
                WebElement startWorkButton = wait.until(driver -> {
                    try {
         
                        WebElement btn = card.findElement(By.xpath(".//button[contains(text(), 'Start Work')]"));
                        return btn.isEnabled() && btn.isDisplayed() && 
                               (btn.getAttribute("disabled") == null || !btn.getAttribute("disabled").equals("true")) ?
                               btn : null;
                    } catch (Exception e) {
                        return null;
                    }
                });
                
          
                clickElementWithJavaScript(startWorkButton);
                
           
                checkForJavaScriptErrors();
             
                try {
                    wait.until(driver -> {
                        try {
                            WebElement btn = card.findElement(By.xpath(".//button[contains(text(), 'Start Work')]"));
                            String disabledAttr = btn.getAttribute("disabled");
                            boolean isDisabled = disabledAttr != null && (disabledAttr.equals("true") || disabledAttr.equals(""));
                            return isDisabled || !btn.isEnabled();
                        } catch (Exception e) {
                            return false;
                        }
                    });
                } catch (org.openqa.selenium.TimeoutException e) {
                }
                
       
                try {
                    wait.until(driver -> {
                        try {
                            WebElement btn = card.findElement(By.xpath(".//button[contains(text(), 'Start Work')]"));
                            String disabledAttr = btn.getAttribute("disabled");
                            boolean isEnabled = (disabledAttr == null || !disabledAttr.equals("true")) && btn.isEnabled();
                            return isEnabled;
                        } catch (Exception e) {
                            return false;
                        }
                    });
                } catch (org.openqa.selenium.TimeoutException e) {
           
                }
                
   
                wait.until(driver -> {
                    List<WebElement> refreshedCards = getIssueCards();
                    for (WebElement refreshedCard : refreshedCards) {
                        if (refreshedCard.findElement(ISSUE_TITLE).getText().equals(issueTitle)) {
                            List<WebElement> statusTags = refreshedCard.findElements(STATUS_TAG);
                            return !statusTags.isEmpty() && statusTags.stream().anyMatch(WebElement::isDisplayed);
                        }
                    }
                    return false;
                });
                
                WebDriverWait statusWait = new WebDriverWait(driver, Duration.ofSeconds(20));
                statusWait.until(driver -> {
                   
                    List<WebElement> refreshedCards = getIssueCards();
                    for (WebElement refreshedCard : refreshedCards) {
                        if (refreshedCard.findElement(ISSUE_TITLE).getText().equals(issueTitle)) {
                            String newStatus = getIssueStatusFromCard(refreshedCard);
                            if (newStatus == null || newStatus.isEmpty()) {
                                return false;
                            }
                           
                            String statusUpper = newStatus.toUpperCase().replace("_", " ").replace("-", " ").trim();
                            boolean matches = statusUpper.contains("IN PROGRESS") || statusUpper.equals("IN PROGRESS");
                            return matches;
                        }
                    }
                    return false;
                });
                return;
            }
        }
    }
    
    private String getIssueStatusFromCard(WebElement card) {
        try {
            List<WebElement> tags = card.findElements(STATUS_TAG);
            for (WebElement tag : tags) {
                String text = tag.getText();
                String textUpper = text.toUpperCase().trim();
                // Frontend displays status with spaces (e.g., "IN PROGRESS"), check for various formats
                if (textUpper.contains("IN PROGRESS") || textUpper.equals("IN PROGRESS") ||
                    textUpper.contains("IN_PROGRESS") || textUpper.equals("IN_PROGRESS") ||
                    textUpper.contains("RESOLVED") || textUpper.equals("RESOLVED") ||
                    textUpper.contains("OPEN") || textUpper.equals("OPEN") ||
                    textUpper.contains("CLOSED") || textUpper.equals("CLOSED") ||
                    textUpper.contains("PENDING") || textUpper.equals("PENDING")) {
                    // Return the text as-is (with spaces) since that's how frontend displays it
                    return text.trim();
                }
            }
        } catch (Exception e) {
      
        }
        return "";
    }

    public void clickMarkResolvedForIssue(String issueTitle) {
        List<WebElement> issueCards = getIssueCards();
        for (WebElement card : issueCards) {
            if (card.findElement(ISSUE_TITLE).getText().equals(issueTitle)) {
          
                WebElement resolvedButton = wait.until(driver -> {
                    try {
                
                        WebElement btn = card.findElement(By.xpath(".//button[contains(text(), 'Mark as Resolved')]"));
                        return btn.isEnabled() && btn.isDisplayed() && 
                               (btn.getAttribute("disabled") == null || !btn.getAttribute("disabled").equals("true")) ?
                               btn : null;
                    } catch (Exception e) {
                        return null;
                    }
                });
                

                clickElementWithJavaScript(resolvedButton);
                
             
                checkForJavaScriptErrors();
             
                try {
                    wait.until(driver -> {
                        try {
                            WebElement btn = card.findElement(By.xpath(".//button[contains(text(), 'Mark as Resolved')]"));
                            String disabledAttr = btn.getAttribute("disabled");
                            return (disabledAttr != null && (disabledAttr.equals("true") || disabledAttr.equals(""))) || !btn.isEnabled();
                        } catch (Exception e) {
                            return false;
                        }
                    });
                } catch (org.openqa.selenium.TimeoutException e) {
         
                }
            
                try {
                    wait.until(driver -> {
                        try {
                            WebElement btn = card.findElement(By.xpath(".//button[contains(text(), 'Mark as Resolved')]"));
                            String disabledAttr = btn.getAttribute("disabled");
                            return (disabledAttr == null || !disabledAttr.equals("true")) && btn.isEnabled();
                        } catch (Exception e) {
                            return false;
                        }
                    });
                } catch (org.openqa.selenium.TimeoutException e) {
               
                }
                
              
                wait.until(driver -> {
                    List<WebElement> refreshedCards = getIssueCards();
                    for (WebElement refreshedCard : refreshedCards) {
                        if (refreshedCard.findElement(ISSUE_TITLE).getText().equals(issueTitle)) {
                            List<WebElement> statusTags = refreshedCard.findElements(STATUS_TAG);
                            return !statusTags.isEmpty() && statusTags.stream().anyMatch(WebElement::isDisplayed);
                        }
                    }
                    return false;
                });
                
             
                WebDriverWait statusWait = new WebDriverWait(driver, Duration.ofSeconds(20));
                statusWait.until(driver -> {
              
                    List<WebElement> refreshedCards = getIssueCards();
                    for (WebElement refreshedCard : refreshedCards) {
                        if (refreshedCard.findElement(ISSUE_TITLE).getText().equals(issueTitle)) {
                            String newStatus = getIssueStatusFromCard(refreshedCard);
                            if (newStatus == null || newStatus.isEmpty()) {
                                return false;
                            }
                     
                            String statusUpper = newStatus.toUpperCase().replace("_", " ").replace("-", " ").trim();
                            return statusUpper.contains("RESOLVED") || statusUpper.equals("RESOLVED");
                        }
                    }
                    return false;
                });
                return;
            }
        }
    }

    public String getIssueStatus(String issueTitle) {
        List<WebElement> issueCards = getIssueCards();
        for (WebElement card : issueCards) {
            try {
                if (card.findElement(ISSUE_TITLE).getText().equals(issueTitle)) {
                    List<WebElement> tags = card.findElements(STATUS_TAG);
                    for (WebElement tag : tags) {
                        String text = tag.getText();
                        String textUpper = text.toUpperCase().trim();
                        // Frontend displays status with spaces (e.g., "IN PROGRESS"), check for various formats
                        if (textUpper.contains("IN PROGRESS") || textUpper.equals("IN PROGRESS") ||
                            textUpper.contains("IN_PROGRESS") || textUpper.equals("IN_PROGRESS") ||
                            textUpper.contains("RESOLVED") || textUpper.equals("RESOLVED") ||
                            textUpper.contains("OPEN") || textUpper.equals("OPEN") ||
                            textUpper.contains("CLOSED") || textUpper.equals("CLOSED") ||
                            textUpper.contains("PENDING") || textUpper.equals("PENDING")) {
                            // Return the text as-is (with spaces) since that's how frontend displays it
                            return text.trim();
                        }
                    }
                    // check for status in other common locations (span, div, etc.)
                    try {
                        WebElement statusElement = card.findElement(By.className("status"));
                        String statusText = statusElement.getText();
                        return statusText;
                    } catch (Exception e) {
          
                    }
                }
            } catch (Exception e) {
             
            }
        }
        return "";
    }

    public int getIssueCount() {
        return getIssueCards().size();
    }
}
