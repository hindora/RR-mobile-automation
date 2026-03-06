package com.rr.mobile.utils;

import com.rr.mobile.config.FrameworkConfig;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Centralised explicit-wait helpers built on top of WebDriverWait / FluentWait.
 * Each BasePage holds one instance; tests never call Thread.sleep() directly.
 */
public class WaitUtils {

    private static final Logger log = LogManager.getLogger(WaitUtils.class);

    private final AndroidDriver driver;
    private final WebDriverWait defaultWait;

    public WaitUtils(AndroidDriver driver) {
        this.driver = driver;
        this.defaultWait = new WebDriverWait(
            driver, Duration.ofSeconds(FrameworkConfig.DEFAULT_EXPLICIT_WAIT));
    }

    // ---------------------------------------------------------------
    // Visibility / presence
    // ---------------------------------------------------------------

    public WebElement waitForElementVisible(By locator) {
        log.debug("Waiting for visible: {}", locator);
        return defaultWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElementVisible(By locator, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElementPresent(By locator) {
        log.debug("Waiting for presence: {}", locator);
        return defaultWait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public List<WebElement> waitForAllElementsVisible(By locator) {
        log.debug("Waiting for all elements visible: {}", locator);
        return defaultWait.until(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    // ---------------------------------------------------------------
    // Clickability
    // ---------------------------------------------------------------

    public WebElement waitForElementClickable(By locator) {
        log.debug("Waiting for clickable: {}", locator);
        return defaultWait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement waitForElementClickable(WebElement element) {
        return defaultWait.until(ExpectedConditions.elementToBeClickable(element));
    }

    // ---------------------------------------------------------------
    // Invisibility / text
    // ---------------------------------------------------------------

    public boolean waitForElementInvisible(By locator) {
        log.debug("Waiting for invisible: {}", locator);
        return defaultWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public boolean waitForTextPresent(By locator, String text) {
        log.debug("Waiting for text '{}' in: {}", text, locator);
        return defaultWait.until(
            ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    // ---------------------------------------------------------------
    // FluentWait (ignores transient exceptions, configurable polling)
    // ---------------------------------------------------------------

    public WebElement fluentWait(By locator) {
        return fluentWait(locator,
            FrameworkConfig.DEFAULT_EXPLICIT_WAIT,
            FrameworkConfig.FLUENT_WAIT_POLLING);
    }

    public WebElement fluentWait(By locator, int timeoutSeconds, int pollingSeconds) {
        FluentWait<AndroidDriver> wait = new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(timeoutSeconds))
            .pollingEvery(Duration.ofSeconds(pollingSeconds))
            .ignoring(NoSuchElementException.class)
            .ignoring(StaleElementReferenceException.class);
        return wait.until(d -> d.findElement(locator));
    }

    // ---------------------------------------------------------------
    // Utility
    // ---------------------------------------------------------------

    /**
     * Checks whether an element is present in DOM right now (no wait).
     * Useful for conditional branching without throwing an exception.
     */
    public boolean isElementPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }
}
