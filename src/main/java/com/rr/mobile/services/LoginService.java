package com.rr.mobile.services;

import com.rr.mobile.pages.HomePage;
import com.rr.mobile.pages.LoginPage;
import com.rr.mobile.utils.JsonDataReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Business-logic layer for login flows.
 *
 * Tests call service methods rather than page methods directly,
 * keeping test code free of UI-level details.
 *
 * A new LoginService should be created per test method (not shared
 * across tests) to get a fresh LoginPage bound to the current thread's driver.
 */
public class LoginService {

    private static final Logger log = LogManager.getLogger(LoginService.class);

    private static final String TEST_DATA_FILE = "testdata.json";

    private final LoginPage loginPage;

    public LoginService() {
        this.loginPage = new LoginPage();
    }

    // ---------------------------------------------------------------
    // Happy-path login
    // ---------------------------------------------------------------

    /**
     * Logs in with the supplied credentials and returns the resulting HomePage.
     *
     * @throws RuntimeException propagated from Appium if the home screen never appears
     */
    public HomePage performLogin(String username, String password) {
        log.info("performLogin: user={}", username);
        return loginPage.loginSuccessfully(username, password);
    }

    /**
     * Reads credentials from the JSON test-data file and performs a successful login.
     *
     * @param dataSetKey top-level key in testdata.json, e.g. {@code "validUser"}
     */
    public HomePage loginWithDataSet(String dataSetKey) {
        Map<String, String> data = JsonDataReader.getDataSet(TEST_DATA_FILE, dataSetKey);
        log.info("loginWithDataSet: key={}, user={}", dataSetKey, data.get("username"));
        return performLogin(data.get("username"), data.get("password"));
    }

    // ---------------------------------------------------------------
    // Negative-path login
    // ---------------------------------------------------------------

    /**
     * Attempts a login expected to fail and returns whether an error message appeared.
     */
    public boolean attemptInvalidLogin(String username, String password) {
        log.info("attemptInvalidLogin: user={}", username);
        loginPage.loginExpectingError(username, password);
        return loginPage.isErrorMessageDisplayed();
    }

    /**
     * Returns the error message text currently shown on the login screen.
     */
    public String getLoginErrorMessage() {
        return loginPage.getErrorMessage();
    }

    // ---------------------------------------------------------------
    // Checks
    // ---------------------------------------------------------------

    public boolean isLoginPageLoaded() {
        return loginPage.isPageLoaded();
    }

    public boolean wasLoginSuccessToastSeen() {
        return loginPage.wasLoginSuccessToastSeen();
    }

    public LoginPage getLoginPage() {
        return loginPage;
    }
}
