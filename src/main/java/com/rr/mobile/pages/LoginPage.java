package com.rr.mobile.pages;

import com.rr.mobile.base.BasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Page Object for the Login screen.
 *
 * Update the resource-id constants below to match your actual app's IDs.
 * Run:  uiautomatorviewer  or  Appium Inspector  to inspect element IDs.
 */
public class LoginPage extends BasePage {

    // ---------------------------------------------------------------
    // Locators — app has no resource-ids; using content-desc / XPath
    // ---------------------------------------------------------------
    private static final By USERNAME_FIELD     = By.xpath("(//android.widget.EditText)[1]");
    private static final By PASSWORD_FIELD     = By.xpath("(//android.widget.EditText)[2]");
    private static final By LOGIN_BUTTON       = AppiumBy.accessibilityId("Login to Fleet");
    private static final By LOGIN_SUCCESS_TOAST = By.xpath(
        "//*[contains(@text,'success') or contains(@text,'Success')" +
        " or contains(@content-desc,'success') or contains(@content-desc,'Login successful')]");

    private boolean loginSuccessToastSeen = false;
    private static final By ERROR_MESSAGE   = By.xpath("//android.view.View[contains(@content-desc,'error') or contains(@content-desc,'invalid') or contains(@content-desc,'incorrect')]");
    private static final By FORGOT_PASSWORD = AppiumBy.accessibilityId("Forgot password?");
    private static final By REGISTER_LINK   = AppiumBy.accessibilityId("New to the fleet? \nSign Up");
    private static final By APP_LOGO        = AppiumBy.accessibilityId("Login");

    public LoginPage() {
        super();
        log.info("LoginPage ready");
    }

    // ---------------------------------------------------------------
    // Page contract
    // ---------------------------------------------------------------

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(LOGIN_BUTTON);
    }

    // ---------------------------------------------------------------
    // Actions – fluent API so callers can chain
    // ---------------------------------------------------------------

    public LoginPage enterUsername(String username) {
        log.info("Entering username: {}", username);
        type(USERNAME_FIELD, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        log.info("Entering password");
        type(PASSWORD_FIELD, password);
        return this;
    }

    public LoginPage clearUsername() {
        findElement(USERNAME_FIELD).clear();
        return this;
    }

    public LoginPage clearPassword() {
        findElement(PASSWORD_FIELD).clear();
        return this;
    }

    /**
     * Taps Login and returns a new HomePage (use when credentials are valid).
     */
    public HomePage clickLogin() {
        log.info("Tapping Login button");
        hideKeyboard();
        tap(LOGIN_BUTTON);
        loginSuccessToastSeen = isPresent(LOGIN_SUCCESS_TOAST, 5);
        log.info("Login success toast seen: {}", loginSuccessToastSeen);
        return new HomePage();
    }

    public boolean wasLoginSuccessToastSeen() {
        return loginSuccessToastSeen;
    }

    /**
     * Taps Login and stays on LoginPage (use when credentials are invalid
     * or when validating error messages).
     */
    public LoginPage clickLoginExpectingError() {
        log.info("Tapping Login button (expecting error)");
        hideKeyboard();
        tap(LOGIN_BUTTON);
        return this;
    }

    // ---------------------------------------------------------------
    // Composite login flows
    // ---------------------------------------------------------------

    /** Full happy-path login – returns HomePage on success. */
    public HomePage loginSuccessfully(String username, String password) {
        return enterUsername(username)
               .enterPassword(password)
               .clickLogin();
    }

    /** Login flow that keeps the page (expected to show an error). */
    public LoginPage loginExpectingError(String username, String password) {
        return enterUsername(username)
               .enterPassword(password)
               .clickLoginExpectingError();
    }

    // ---------------------------------------------------------------
    // Assertions helpers
    // ---------------------------------------------------------------

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(ERROR_MESSAGE);
    }

    public boolean isLoginButtonEnabled() {
        return isEnabled(LOGIN_BUTTON);
    }

    public boolean isLogoDisplayed() {
        return isDisplayed(APP_LOGO);
    }

    // ---------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------

    public void tapForgotPassword() {
        log.info("Tapping Forgot Password");
        tap(FORGOT_PASSWORD);
    }

    public void tapRegister() {
        log.info("Tapping Register");
        tap(REGISTER_LINK);
    }
}
