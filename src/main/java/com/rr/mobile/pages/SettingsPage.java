package com.rr.mobile.pages;

import com.rr.mobile.base.BasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Page Object for the Settings screen.
 */
public class SettingsPage extends BasePage {

    private static final By SETTINGS_HEADER       = AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Preferences & Configuration\")");
    private static final By NOTIFICATIONS_SECTION = AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"NOTIFICATIONS\")");
    private static final By LOCATION_SECTION      = AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"LOCATION\")");
    private static final By DISPLAY_SECTION       = AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"DISPLAY\")");

    public SettingsPage() {
        super();
        log.info("SettingsPage ready");
    }

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(SETTINGS_HEADER);
    }

    public boolean isNotificationsSectionDisplayed() {
        return isDisplayed(NOTIFICATIONS_SECTION);
    }

    public boolean isLocationSectionDisplayed() {
        return isDisplayed(LOCATION_SECTION);
    }

    public boolean isDisplaySectionDisplayed() {
        return isDisplayed(DISPLAY_SECTION);
    }

    public void scrollDown() {
        log.info("Scrolling settings page down");
        gestureUtils.swipeUp();
    }
}
