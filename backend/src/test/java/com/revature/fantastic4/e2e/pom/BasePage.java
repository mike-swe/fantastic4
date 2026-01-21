package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected void waitForElementToBeVisible(By locator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (org.openqa.selenium.TimeoutException e) {
            String currentUrl = getCurrentUrl();
            throw new org.openqa.selenium.TimeoutException(
                "Element not visible: " + locator + ". Current URL: " + currentUrl, e);
        }
    }

    protected void waitForElementToBeClickable(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void waitForElementToDisappear(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public void waitForUrlToContain(String urlFragment) {
        wait.until(ExpectedConditions.urlContains(urlFragment));
    }

    protected void waitForElementCountToChange(By locator, int initialCount) {
        wait.until(driver -> {
            List<WebElement> elements = driver.findElements(locator);
            return elements.size() != initialCount;
        });
    }

    protected void waitForElementTextToContain(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    protected void waitForElementTextToChange(By locator, String oldText) {
        wait.until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                return !element.getText().equals(oldText);
            } catch (Exception e) {
                return false;
            }
        });
    }

    protected void clickElement(By locator) {
        waitForElementToBeClickable(locator);
        driver.findElement(locator).click();
    }

    protected void clickElementWithJavaScript(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void checkForJavaScriptErrors() {
        try {
            Object logs = ((JavascriptExecutor) driver).executeScript(
                "return window.console && window.console.getLogs ? window.console.getLogs() : []");
        } catch (Exception e) {
        }
    }
    protected void enterText(By locator, String text) {
        waitForElementToBeVisible(locator);
        WebElement element = driver.findElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        waitForElementToBeVisible(locator);
        return driver.findElement(locator).getText();
    }

    protected boolean isElementVisible(By locator) {
        try {
            waitForElementToBeVisible(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void navigateTo(String url) {
        driver.get(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
