package com.rr.mobile.tests;

import com.rr.mobile.base.BaseTest;
import com.rr.mobile.listeners.RetryAnalyzer;
import com.rr.mobile.pages.HomePage;
import com.rr.mobile.pages.ProfilePage;
import com.rr.mobile.pages.SettingsPage;
import com.rr.mobile.services.LoginService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Home screen and navigation tests.
 *
 * Each test starts from a fresh driver session (BaseTest.setUp) and
 * logs in via the service layer before exercising home-screen features.
 */
public class HomeTest extends BaseTest {

    private HomePage homePage;
    private boolean loginToastShown;

    @BeforeMethod
    public void login() {
        log.info("Pre-test: logging in");
        LoginService loginService = new LoginService();
        homePage = loginService.loginWithDataSet("validUser");
        loginToastShown = loginService.wasLoginSuccessToastSeen();
    }

    // ---------------------------------------------------------------
    // TC-H01  Home page load
    // ---------------------------------------------------------------

    @Test(description = "Verify Home page is displayed after login",
          retryAnalyzer = RetryAnalyzer.class)
    public void testHomePageIsDisplayedAfterLogin() {
        logStep("Check Home page is fully loaded");
        Assert.assertTrue(homePage.isPageLoaded(),
            "Home page should be visible after login");
    }

    // ---------------------------------------------------------------
    // TC-H02  Welcome message
    // ---------------------------------------------------------------

    @Test(description = "Verify welcome message is displayed on Home page",
          retryAnalyzer = RetryAnalyzer.class)
    public void testWelcomeMessageIsDisplayed() {
        logStep("Verify login success toast was shown");
        Assert.assertTrue(loginToastShown,
            "Login success toast should have appeared after login");

        logStep("Read welcome message from Home page");
        String welcome = homePage.getWelcomeMessage();
        log.info("Welcome message text: {}", welcome);

        logStep("Verify welcome message contains a time-of-day greeting");
        boolean hasGreeting = welcome.contains("Good morning")
                           || welcome.contains("Good afternoon")
                           || welcome.contains("Good evening");
        Assert.assertTrue(hasGreeting,
            "Expected 'Good morning/afternoon/evening' but got: " + welcome);

        logStep("Verify welcome message contains today's date");
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        Assert.assertTrue(welcome.contains(today),
            "Expected date '" + today + "' in welcome message but got: " + welcome);
    }

    // ---------------------------------------------------------------
    // TC-H03  Navigate to Profile
    // ---------------------------------------------------------------

    @Test(description = "Verify navigation from Home to Profile page",
          retryAnalyzer = RetryAnalyzer.class)
    public void testNavigateToProfilePage() {
        logStep("Tap S profile button and select My Profile");
        ProfilePage profilePage = homePage.navigateToProfile();

        logStep("Verify Personal Information section is displayed");
        Assert.assertTrue(profilePage.isPersonalInformationSectionDisplayed(),
            "Personal Information section should be visible on Profile page");

        logStep("Scroll down to reveal more sections");
        profilePage.scrollDown();

        logStep("Verify Role & Organization section is displayed");
        Assert.assertTrue(profilePage.isRoleAndOrganizationSectionDisplayed(),
            "Role & Organization section should be visible on Profile page");

        logStep("Verify Account Status section is displayed");
        Assert.assertTrue(profilePage.isAccountStatusSectionDisplayed(),
            "Account Status section should be visible on Profile page");
    }

    // ---------------------------------------------------------------
    // TC-H04  Navigate to Settings
    // ---------------------------------------------------------------

    @Test(description = "Verify navigation from Home to Settings page",
          retryAnalyzer = RetryAnalyzer.class)
    public void testNavigateToSettingsPage() {
        logStep("Tap Settings tab in bottom navigation");
        SettingsPage settingsPage = homePage.navigateToSettings();

        logStep("Verify Settings page is loaded (Preferences & Configuration header)");
        Assert.assertTrue(settingsPage.isPageLoaded(),
            "Settings page should display 'Preferences & Configuration' header");

        logStep("Verify Notifications section is displayed");
        Assert.assertTrue(settingsPage.isNotificationsSectionDisplayed(),
            "NOTIFICATIONS section should be visible on Settings page");

        logStep("Verify Location & Tracking section is displayed");
        Assert.assertTrue(settingsPage.isLocationSectionDisplayed(),
            "LOCATION & TRACKING section should be visible on Settings page");

        logStep("Scroll down to reveal Display section");
        settingsPage.scrollDown();

        logStep("Verify Display section is displayed");
        Assert.assertTrue(settingsPage.isDisplaySectionDisplayed(),
            "DISPLAY section should be visible after scrolling on Settings page");
    }

    // ---------------------------------------------------------------
    // TC-H05  Scroll up/down
    // ---------------------------------------------------------------

    @Test(description = "Verify scroll gestures work on the Home page",
          retryAnalyzer = RetryAnalyzer.class)
    public void testScrollGesturesOnHomePage() {
        logStep("Verify Home page is loaded before scrolling");
        Assert.assertTrue(homePage.isPageLoaded(), "Home page must be loaded");

        logStep("Scroll down (swipe up gesture)");
        homePage.scrollDown();

        logStep("Scroll back up (swipe down gesture)");
        homePage.scrollUp();

        logStep("Verify Home page is still loaded after scrolling");
        Assert.assertTrue(homePage.isPageLoaded(),
            "Home page should still be visible after scrolling");
    }

    // ---------------------------------------------------------------
    // TC-H06  Logout
    // ---------------------------------------------------------------

    @Test(description = "Verify user is redirected to Login page after logout",
          retryAnalyzer = RetryAnalyzer.class)
    public void testLogoutReturnsToLoginPage() {
        logStep("Trigger logout from Home page");
        var loginPage = homePage.logout();

        logStep("Verify Login page is displayed after logout");
        Assert.assertTrue(loginPage.isPageLoaded(),
            "Login page should be visible after logout");
    }
}
