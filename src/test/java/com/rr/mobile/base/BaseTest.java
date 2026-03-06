package com.rr.mobile.base;

import com.aventstack.extentreports.ExtentTest;
import com.rr.mobile.driver.DriverFactory;
import com.rr.mobile.driver.DriverManager;
import com.rr.mobile.listeners.TestListener;
import com.rr.mobile.reporting.ExtentReportManager;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Base class for every test class.
 *
 * Lifecycle:
 *  @BeforeMethod  – spin up an AndroidDriver and store in DriverManager
 *  @AfterMethod   – quit the driver and clean up the ThreadLocal
 *
 * The @Listeners annotation wires TestListener into every subclass
 * so no one needs to repeat it.
 */
@Listeners(TestListener.class)
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        log.info("=== [setUp] Thread: {} ===", Thread.currentThread().getId());
        AndroidDriver driver = DriverFactory.createAndroidDriver();
        DriverManager.setDriver(driver);
        log.info("=== Driver ready ===");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        log.info("=== [tearDown] test='{}' | status={} ===",
            result.getMethod().getMethodName(),
            statusLabel(result.getStatus()));
        try {
            if (DriverManager.isDriverActive()) {
                DriverManager.getDriver().quit();
                log.info("Driver quit successfully");
            }
        } catch (Exception e) {
            log.error("Error during driver quit: {}", e.getMessage());
        } finally {
            DriverManager.unload();
        }
    }

    // ---------------------------------------------------------------
    // Helpers available to all tests
    // ---------------------------------------------------------------

    /**
     * Returns the ExtentTest node for the current test so individual test
     * methods can add custom log entries.
     */
    protected ExtentTest getExtentTest() {
        return ExtentReportManager.getTest();
    }

    /**
     * Logs an atomic test step both to Log4j and to the ExtentReport node.
     */
    protected void logStep(String description) {
        log.info("  STEP: {}", description);
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.info(description);
        }
    }

    // ---------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------

    private String statusLabel(int status) {
        switch (status) {
            case ITestResult.SUCCESS: return "PASS";
            case ITestResult.FAILURE: return "FAIL";
            case ITestResult.SKIP:    return "SKIP";
            default:                  return "UNKNOWN";
        }
    }
}
