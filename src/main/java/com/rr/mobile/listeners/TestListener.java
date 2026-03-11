package com.rr.mobile.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.rr.mobile.driver.DriverManager;
import com.rr.mobile.reporting.ExtentReportManager;
import com.rr.mobile.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.util.Base64;

/**
 * TestNG listener wired into testng.xml and @Listeners on BaseTest.
 *
 * Responsibilities:
 *  1. Create / close ExtentTest nodes per test method.
 *  2. Capture a screenshot and attach it to the report on failure.
 *  3. Flush the report at suite completion.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    // ---------------------------------------------------------------
    // Suite lifecycle
    // ---------------------------------------------------------------

    @Override
    public void onStart(ITestContext context) {
        log.info("====== Suite started: {} ======", context.getName());
        ScreenshotUtils.ensureDirectoryExists();
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("====== Suite finished: {} | Passed: {} | Failed: {} | Skipped: {} ======",
            context.getName(),
            context.getPassedTests().size(),
            context.getFailedTests().size(),
            context.getSkippedTests().size());
        ExtentReportManager.flushReport();
    }

    // ---------------------------------------------------------------
    // Test lifecycle
    // ---------------------------------------------------------------

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        log.info("---> TEST START: {}", name);
        ExtentReportManager.createTest(name, desc);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("---> TEST PASS:  {}", result.getMethod().getMethodName());
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.pass("Test passed");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String name = result.getMethod().getMethodName();
        log.error("---> TEST FAIL:  {} | {}", name,
            result.getThrowable() != null ? result.getThrowable().getMessage() : "unknown");

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.fail(result.getThrowable());
            attachScreenshotToReport(test, name);
        }
        // Always save a file copy for later inspection
        ScreenshotUtils.captureScreenshot(name);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String name = result.getMethod().getMethodName();
        log.warn("---> TEST SKIP:  {}", name);
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            String reason = result.getThrowable() != null
                ? result.getThrowable().getMessage() : "No reason provided";
            test.skip("Test skipped: " + reason);
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.warn("Test failed within success percentage: {}", result.getMethod().getMethodName());
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private void attachScreenshotToReport(ExtentTest test, String testName) {
        if (!DriverManager.isDriverActive()) {
            return;
        }
        try {
            byte[] bytes = ScreenshotUtils.captureScreenshotAsBytes();
            if (bytes.length > 0) {
                String base64 = Base64.getEncoder().encodeToString(bytes);
                // Attach to ExtentReports (Spark HTML report)
                test.fail("Screenshot at failure",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
                // Embed in testng-results.xml so Jenkins TestNG Results plugin shows the screenshot
                Reporter.log("<br/><b>Failure Screenshot:</b><br/>" +
                    "<img src='data:image/png;base64," + base64 +
                    "' style='max-width:900px;border:2px solid #e74c3c;'/><br/>", true);
            }
        } catch (Exception e) {
            log.error("Could not attach screenshot to report: {}", e.getMessage());
        }
    }
}
