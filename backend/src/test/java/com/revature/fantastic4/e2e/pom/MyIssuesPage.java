package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class MyIssuesPage extends BasePage {

    private static final By ISSUE_CARDS = By.className("issue-card");
    private static final By ISSUE_TITLE = By.className("issue-title");

    public MyIssuesPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToMyIssues() {
        navigateTo("http://localhost:4200/my-issues");
        waitForElementToBeVisible(ISSUE_CARDS);
    }

    public List<WebElement> getIssueCards() {
        return driver.findElements(ISSUE_CARDS);
    }

    public boolean isIssueVisible(String issueTitle) {
        List<WebElement> issueCards = getIssueCards();
        for (WebElement card : issueCards) {
            try {
                if (card.findElement(ISSUE_TITLE).getText().equals(issueTitle)) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public int getIssueCount() {
        return getIssueCards().size();
    }
}
