package com.rr.mobile.driver;

import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ThreadLocal store for AndroidDriver instances.
 *
 * Each test thread gets its own driver reference, enabling safe
 * parallel execution across multiple devices without synchronisation
 * overhead in test code.
 */
public final class DriverManager {

    private static final Logger log = LogManager.getLogger(DriverManager.class);

    private static final ThreadLocal<AndroidDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    private DriverManager() {}

    /** Store the driver for the current thread. */
    public static void setDriver(AndroidDriver driver) {
        DRIVER_THREAD_LOCAL.set(driver);
        log.debug("Driver stored for thread [{}]", Thread.currentThread().getId());
    }

    /**
     * Retrieve the driver for the current thread.
     *
     * @throws IllegalStateException if called before {@link #setDriver(AndroidDriver)}.
     */
    public static AndroidDriver getDriver() {
        AndroidDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver == null) {
            throw new IllegalStateException(
                "No AndroidDriver found for thread [" + Thread.currentThread().getId() + "]. "
                + "Was BaseTest.setUp() executed?");
        }
        return driver;
    }

    /** Remove the driver reference from the current thread (must be called in teardown). */
    public static void unload() {
        DRIVER_THREAD_LOCAL.remove();
        log.debug("Driver removed from thread [{}]", Thread.currentThread().getId());
    }

    /** Safe check – avoids the exception from {@link #getDriver()} in listener code. */
    public static boolean isDriverActive() {
        return DRIVER_THREAD_LOCAL.get() != null;
    }
}
