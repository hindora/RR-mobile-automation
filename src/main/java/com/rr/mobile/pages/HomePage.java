package com.rr.mobile.pages;

import com.rr.mobile.base.BasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Page Object for the Home / Dashboard screen.
 */
public class HomePage extends BasePage {

    // ---------------------------------------------------------------
    // Locators
    // ---------------------------------------------------------------
    private static final By FLEET_TAB         = AppiumBy.androidUIAutomator("new UiSelector().description(\"Fleet\")");
    private static final By WELCOME_MESSAGE   = AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Good\")");
    private static final By PROFILE_BUTTON    = AppiumBy.androidUIAutomator("new UiSelector().description(\"S\")");
    private static final By MY_PROFILE_OPTION = AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"My Profile\")");

    public HomePage() {
        super();
        log.info("HomePage ready");
    }

    // ---------------------------------------------------------------
    // Page contract
    // ---------------------------------------------------------------

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(FLEET_TAB);
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------

    public String getWelcomeMessage() {
        return getAttribute(WELCOME_MESSAGE, "content-desc");
    }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    public void scrollDown() {
        log.info("Scrolling down");
        gestureUtils.swipeUp();
    }

    public void scrollUp() {
        log.info("Scrolling up");
        gestureUtils.swipeDown();
    }

    // ---------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------

    public ProfilePage navigateToProfile() {
        log.info("Navigating → Profile via S button");
        tap(PROFILE_BUTTON);
        tap(MY_PROFILE_OPTION);
        return new ProfilePage();
    }

    public SettingsPage navigateToSettings() {
        log.info("Navigating → Settings");
        tap(AppiumBy.androidUIAutomator("new UiSelector().description(\"Settings\")"));
        return new SettingsPage();
    }

    public LoginPage logout() {
        log.info("Logging out");
        tap(PROFILE_BUTTON);
        tap(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"sign out\")"));
        tap(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Logout\").clickable(true)"));
        return new LoginPage();
    }
}
