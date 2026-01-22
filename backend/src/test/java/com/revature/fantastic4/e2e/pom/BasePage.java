package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.aop.framework.Advised;

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

    /**
     * Safely obtains a JavascriptExecutor from the WebDriver, handling Spring proxy cases.
     * Spring creates proxies for @ScenarioScope beans, which don't directly implement
     * JavascriptExecutor even though the underlying WebDriver does.
     */
    protected JavascriptExecutor getJavascriptExecutor() {
        // if driver is already a JavascriptExecutor (non-proxied case)
        if (driver instanceof JavascriptExecutor) {
            return (JavascriptExecutor) driver;
        }
        
        // Unwrap Spring proxy to get the underlying WebDriver
        if (driver instanceof Advised) {
            try {
                Object target = ((Advised) driver).getTargetSource().getTarget();
                if (target instanceof JavascriptExecutor) {
                    return (JavascriptExecutor) target;
                }
                
                throw new RuntimeException(
                    "Unwrapped WebDriver is not a JavascriptExecutor. " +
                    "Driver class: " + driver.getClass().getName() + 
                    ", Target class: " + target.getClass().getName());
            } catch (Exception e) {
                throw new RuntimeException(
                    "Unable to unwrap Spring proxy to get target WebDriver. " +
                    "Driver class: " + driver.getClass().getName(), e);
            }
        }
        
        // Not a proxy and not a JavascriptExecutor 
        throw new RuntimeException(
            "WebDriver is not a JavascriptExecutor and is not a Spring proxy. " +
            "Driver class: " + driver.getClass().getName());
    }

    protected void clickElementWithJavaScript(WebElement element) {
        getJavascriptExecutor().executeScript("arguments[0].click();", element);
    }

    protected void checkForJavaScriptErrors() {
        try {
            getJavascriptExecutor().executeScript(
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
