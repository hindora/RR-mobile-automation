package com.rr.mobile.utils;

import com.rr.mobile.config.FrameworkConfig;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Reusable gesture implementations for Android using the W3C Actions API
 * (Appium 2.x / java-client 8.x).
 *
 * Prefer these over raw driver.perform() calls so that gesture logic
 * stays out of page objects.
 */
public class GestureUtils {

    private static final Logger log = LogManager.getLogger(GestureUtils.class);
    private static final String FINGER = "finger";

    private final AndroidDriver driver;

    public GestureUtils(AndroidDriver driver) {
        this.driver = driver;
    }

    // ---------------------------------------------------------------
    // Core swipe (all other swipe helpers delegate here)
    // ---------------------------------------------------------------

    /**
     * Performs a swipe from (startX, startY) → (endX, endY).
     *
     * @param durationMs how long the pointer is in motion (larger = slower)
     */
    public void swipe(int startX, int startY, int endX, int endY, long durationMs) {
        log.debug("Swipe ({},{}) → ({},{}) in {}ms", startX, startY, endX, endY, durationMs);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, FINGER);
        Sequence swipe = new Sequence(finger, 1)
            .addAction(finger.createPointerMove(
                Duration.ZERO, PointerInput.Origin.viewport(), startX, startY))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerMove(
                Duration.ofMillis(durationMs), PointerInput.Origin.viewport(), endX, endY))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(swipe));
    }

    // ---------------------------------------------------------------
    // Directional swipes using viewport percentages
    // ---------------------------------------------------------------

    /** Swipe upward (scroll down the page content). */
    public void swipeUp() {
        Dimension size = driver.manage().window().getSize();
        int x      = size.width / 2;
        int startY = (int) (size.height * 0.80);
        int endY   = (int) (size.height * 0.20);
        swipe(x, startY, x, endY, FrameworkConfig.SWIPE_DURATION_MS);
    }

    /** Swipe downward (scroll up the page content). */
    public void swipeDown() {
        Dimension size = driver.manage().window().getSize();
        int x      = size.width / 2;
        int startY = (int) (size.height * 0.20);
        int endY   = (int) (size.height * 0.80);
        swipe(x, startY, x, endY, FrameworkConfig.SWIPE_DURATION_MS);
    }

    /** Swipe leftward (next page in a carousel). */
    public void swipeLeft() {
        Dimension size = driver.manage().window().getSize();
        int y      = size.height / 2;
        int startX = (int) (size.width * 0.80);
        int endX   = (int) (size.width * 0.20);
        swipe(startX, y, endX, y, FrameworkConfig.SWIPE_DURATION_MS);
    }

    /** Swipe rightward (previous page in a carousel). */
    public void swipeRight() {
        Dimension size = driver.manage().window().getSize();
        int y      = size.height / 2;
        int startX = (int) (size.width * 0.20);
        int endX   = (int) (size.width * 0.80);
        swipe(startX, y, endX, y, FrameworkConfig.SWIPE_DURATION_MS);
    }

    // ---------------------------------------------------------------
    // Tap helpers
    // ---------------------------------------------------------------

    /** Tap at an absolute coordinate. */
    public void tap(int x, int y) {
        log.debug("Tap ({},{})", x, y);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, FINGER);
        Sequence tap = new Sequence(finger, 1)
            .addAction(finger.createPointerMove(
                Duration.ZERO, PointerInput.Origin.viewport(), x, y))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(tap));
    }

    /** Tap on the centre of the given element. */
    public void tapOnElement(WebElement element) {
        Point     loc  = element.getLocation();
        Dimension size = element.getSize();
        tap(loc.x + size.width / 2, loc.y + size.height / 2);
    }

    /** Double-tap at absolute coordinates. */
    public void doubleTap(int x, int y) {
        log.debug("Double-tap ({},{})", x, y);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, FINGER);
        Sequence doubleTap = new Sequence(finger, 1)
            .addAction(finger.createPointerMove(
                Duration.ZERO, PointerInput.Origin.viewport(), x, y))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(doubleTap));
    }

    // ---------------------------------------------------------------
    // Long press
    // ---------------------------------------------------------------

    /** Long-press at absolute coordinates for {@code durationMs} milliseconds. */
    public void longPress(int x, int y, long durationMs) {
        log.debug("Long-press ({},{}) for {}ms", x, y, durationMs);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, FINGER);
        Sequence longPress = new Sequence(finger, 1)
            .addAction(finger.createPointerMove(
                Duration.ZERO, PointerInput.Origin.viewport(), x, y))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerMove(
                Duration.ofMillis(durationMs), PointerInput.Origin.viewport(), x, y))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Arrays.asList(longPress));
    }

    /** Long-press on the centre of the given element. */
    public void longPressOnElement(WebElement element, long durationMs) {
        Point     loc  = element.getLocation();
        Dimension size = element.getSize();
        longPress(loc.x + size.width / 2, loc.y + size.height / 2, durationMs);
    }

    /** Long-press on an element using the default duration from FrameworkConfig. */
    public void longPressOnElement(WebElement element) {
        longPressOnElement(element, FrameworkConfig.LONG_PRESS_DURATION_MS);
    }

    // ---------------------------------------------------------------
    // Scroll helpers
    // ---------------------------------------------------------------

    /**
     * Scrolls until the element with the given visible text is on screen.
     * Uses UIAutomator2's UiScrollable – works on any scrollable container.
     */
    public void scrollToText(String text) {
        log.debug("Scrolling to text: '{}'", text);
        String uiScrollable = String.format(
            "new UiScrollable(new UiSelector().scrollable(true))"
            + ".scrollIntoView(new UiSelector().text(\"%s\"))", text);
        driver.findElement(AppiumBy.androidUIAutomator(uiScrollable));
    }

    /**
     * Scrolls until the element with the given content-description is on screen.
     */
    public void scrollToContentDesc(String contentDesc) {
        log.debug("Scrolling to content-desc: '{}'", contentDesc);
        String uiScrollable = String.format(
            "new UiScrollable(new UiSelector().scrollable(true))"
            + ".scrollIntoView(new UiSelector().description(\"%s\"))", contentDesc);
        driver.findElement(AppiumBy.androidUIAutomator(uiScrollable));
    }

    /**
     * Uses the Appium 2.x {@code mobile: scrollGesture} extension.
     *
     * @param elementId  WDIO-style element id (pass {@code null} to scroll the viewport)
     * @param direction  "up" | "down" | "left" | "right"
     * @param percent    fraction of the element height/width to scroll (0.0–1.0)
     */
    public void scrollWithMobileGesture(String elementId, String direction, double percent) {
        log.debug("mobile:scrollGesture element={}, direction={}, percent={}", elementId, direction, percent);
        Map<String, Object> args = new HashMap<>();
        if (elementId != null) {
            args.put("elementId", elementId);
        }
        args.put("direction", direction);
        args.put("percent", percent);
        args.put("speed", 1500);
        driver.executeScript("mobile: scrollGesture", args);
    }
}
