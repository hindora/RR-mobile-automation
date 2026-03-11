package com.rr.mobile.listeners;

import com.rr.mobile.driver.DriverManager;
import com.rr.mobile.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.util.Base64;
import java.util.Collection;

/**
 * TestNG listener wired into testng.xml and @Listeners on BaseTest.
 *
 * Responsibilities:
 *  1. Log test lifecycle events via Log4j.
 *  2. Capture a screenshot and embed it in the TestNG reporter on failure.
 *  3. Inject a pass-rate summary into every test result at suite completion.
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
        int passed  = context.getPassedTests().size();
        int failed  = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total   = passed + failed + skipped;
        String passRate = total > 0
            ? String.format("%.1f%%", (passed * 100.0) / total)
            : "N/A";

        log.info("====== Suite finished: {} | Passed: {} | Failed: {} | Skipped: {} | Pass Rate: {} ======",
            context.getName(), passed, failed, skipped, passRate);

        // Inject pass-rate summary into every test result's reporter output so it
        // appears on the Jenkins TestNG Results plugin method-detail page.
        String colour  = failed == 0 ? "#2ecc71"
                       : (total > 0 && (passed * 100.0 / total) >= 80.0) ? "#f39c12" : "#e74c3c";
        String summary =
            "<hr style='border:1px solid #ccc;'/>" +
            "<table style='font-size:13px;border-collapse:collapse;'>" +
            "<tr><td style='padding:2px 8px;'><b>Suite Pass Rate</b></td>" +
            "<td style='padding:2px 8px;color:" + colour + ";font-weight:bold;font-size:15px;'>" + passRate + "</td></tr>" +
            "<tr><td style='padding:2px 8px;color:#2ecc71;'>&#10003; Passed</td><td style='padding:2px 8px;'>" + passed  + "</td></tr>" +
            "<tr><td style='padding:2px 8px;color:#e74c3c;'>&#10007; Failed</td><td style='padding:2px 8px;'>" + failed  + "</td></tr>" +
            "<tr><td style='padding:2px 8px;color:#f39c12;'>&#9702; Skipped</td><td style='padding:2px 8px;'>" + skipped + "</td></tr>" +
            "<tr><td style='padding:2px 8px;'>Total</td><td style='padding:2px 8px;'>" + total + "</td></tr>" +
            "</table>" +
            "<hr style='border:1px solid #ccc;'/>";

        attachSummaryToResults(context.getPassedTests().getAllResults(),  summary);
        attachSummaryToResults(context.getFailedTests().getAllResults(),  summary);
        attachSummaryToResults(context.getSkippedTests().getAllResults(), summary);
        Reporter.setCurrentTestResult(null);
    }

    // ---------------------------------------------------------------
    // Test lifecycle
    // ---------------------------------------------------------------

    @Override
    public void onTestStart(ITestResult result) {
        log.info("---> TEST START: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("---> TEST PASS:  {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String name = result.getMethod().getMethodName();
        log.error("---> TEST FAIL:  {} | {}", name,
            result.getThrowable() != null ? result.getThrowable().getMessage() : "unknown");

        attachScreenshotToReporter(result, name);
        ScreenshotUtils.captureScreenshot(name);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("---> TEST SKIP:  {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.warn("Test failed within success percentage: {}", result.getMethod().getMethodName());
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private void attachSummaryToResults(Collection<ITestResult> results, String summaryHtml) {
        for (ITestResult result : results) {
            Reporter.setCurrentTestResult(result);
            Reporter.log(summaryHtml, false);
        }
    }

    private void attachScreenshotToReporter(ITestResult result, String testName) {
        if (!DriverManager.isDriverActive()) {
            return;
        }
        try {
            byte[] bytes = ScreenshotUtils.captureScreenshotAsBytes();
            if (bytes.length > 0) {
                String base64 = Base64.getEncoder().encodeToString(bytes);
                Reporter.setCurrentTestResult(result);
                Reporter.log("<br/><b>Failure Screenshot:</b><br/>" +
                    "<img src='data:image/png;base64," + base64 +
                    "' style='max-width:900px;border:2px solid #e74c3c;'/><br/>", true);
            }
        } catch (Exception e) {
            log.error("Could not attach screenshot to reporter: {}", e.getMessage());
        }
    }
}