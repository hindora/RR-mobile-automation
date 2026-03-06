package com.rr.mobile.pages;

import com.rr.mobile.base.BasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Page Object for the My Profile screen.
 */
public class ProfilePage extends BasePage {

    private static final By PERSONAL_INFO_SECTION  = AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Personal Information\")");
    private static final By ROLE_ORG_SECTION       = AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Role & Organization\")");
    private static final By ACCOUNT_STATUS_SECTION = AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"Account Status\")");

    public ProfilePage() {
        super();
        log.info("ProfilePage ready");
    }

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(PERSONAL_INFO_SECTION);
    }

    public boolean isPersonalInformationSectionDisplayed() {
        return isDisplayed(PERSONAL_INFO_SECTION);
    }

    public boolean isRoleAndOrganizationSectionDisplayed() {
        return isDisplayed(ROLE_ORG_SECTION);
    }

    public boolean isAccountStatusSectionDisplayed() {
        return isDisplayed(ACCOUNT_STATUS_SECTION);
    }

    public void scrollDown() {
        log.info("Scrolling profile page down");
        gestureUtils.swipeUp();
    }
}
