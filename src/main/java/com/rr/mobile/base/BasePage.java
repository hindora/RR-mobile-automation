package com.rr.mobile.base;

import com.rr.mobile.driver.DriverManager;
import com.rr.mobile.utils.GestureUtils;
import com.rr.mobile.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Base class for all Page Objects.
 *
 * Provides:
 *  - Protected driver / waitUtils / gestureUtils instances (no raw driver calls in pages)
 *  - Convenience wrappers (tap, type, getText …) that every page inherits
 *  - Contract method isPageLoaded() enforced on every concrete page
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());

    protected final AndroidDriver driver;
    protected final WaitUtils     waitUtils;
    protected final GestureUtils  gestureUtils;

    protected BasePage() {
        this.driver       = DriverManager.getDriver();
        this.waitUtils    = new WaitUtils(driver);
        this.gestureUtils = new GestureUtils(driver);
    }

    // ---------------------------------------------------------------
    // Contract
    // ---------------------------------------------------------------

    /**
     * Concrete pages must implement this to confirm the page is fully loaded
     * before interactions begin.
     */
    public abstract boolean isPageLoaded();

    // ---------------------------------------------------------------
    // Element finders
    // ---------------------------------------------------------------

    protected WebElement findElement(By locator) {
        return waitUtils.waitForElementVisible(locator);
    }

    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    // ---------------------------------------------------------------
    // Interactions
    // ---------------------------------------------------------------

    protected void tap(By locator) {
        log.debug("Tap: {}", locator);
        waitUtils.waitForElementClickable(locator).click();
    }

    protected void type(By locator, String text) {
        log.debug("Type '{}' → {}", text, locator);
        findElement(locator).click();
        driver.executeScript("mobile: type", Map.of("text", text));
    }

    protected String getText(By locator) {
        String text = findElement(locator).getText();
        log.debug("getText '{}' ← {}", text, locator);
        return text;
    }

    protected String getAttribute(By locator, String attribute) {
        return findElement(locator).getAttribute(attribute);
    }

    // ---------------------------------------------------------------
    // State checks
    // ---------------------------------------------------------------

    protected boolean isDisplayed(By locator) {
        try {
            return waitUtils.waitForElementVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isPresent(By locator) {
        try {
            waitUtils.fluentWait(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isPresent(By locator, int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + Duration.ofSeconds(timeoutSeconds).toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (!driver.findElements(locator).isEmpty()) return true;
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    protected boolean isEnabled(By locator) {
        try {
            return findElement(locator).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isSelected(By locator) {
        try {
            return findElement(locator).isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Keyboard
    // ---------------------------------------------------------------

    protected void hideKeyboard() {
        try {
            if (driver.isKeyboardShown()) {
                driver.pressKey(new KeyEvent(AndroidKey.BACK));
                log.debug("Keyboard hidden via BACK key");
            }
        } catch (Exception e) {
            log.debug("hideKeyboard skipped: {}", e.getMessage());
        }
    }
}
