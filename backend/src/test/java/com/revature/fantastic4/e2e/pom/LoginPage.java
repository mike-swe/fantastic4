package com.revature.fantastic4.e2e.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By USERNAME_INPUT = By.id("username");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.xpath("//button[contains(text(), 'Login')]");
    private static final By ERROR_MESSAGE = By.className("error");
    private static final By CREATE_ACCOUNT_LINK = By.linkText("create account");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToLogin() {
        navigateTo("http://localhost:4200/login");
        waitForElementToBeVisible(USERNAME_INPUT);
    }

    public void enterUsername(String username) {
        enterText(USERNAME_INPUT, username);
    }

    public void enterPassword(String password) {
        enterText(PASSWORD_INPUT, password);
    }

    public void clickLogin() {
        clickElement(LOGIN_BUTTON);
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public boolean isErrorMessageVisible() {
        return isElementVisible(ERROR_MESSAGE);
    }

    public String getErrorMessage() {
        if (isErrorMessageVisible()) {
            return getText(ERROR_MESSAGE);
        }
        return "";
    }

    public boolean isOnLoginPage() {
        return getCurrentUrl().contains("/login");
    }

    public void clickCreateAccountLink() {
        clickElement(CREATE_ACCOUNT_LINK);
    }
}
