package com.rr.mobile.listeners;

import com.rr.mobile.driver.DriverManager;
import com.rr.mobile.utils.ScreenshotUtils;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Collection;

/**
 * TestNG listener wired into testng.xml and @Listeners on BaseTest.
 *
 * Implements both ITestListener and IInvokedMethodListener.
 *
 * Screenshot strategy:
 *   afterInvocation  → fires BEFORE AllureTestNg.onTestFailure closes the test
 *                       case, so Allure.addAttachment() binds correctly.
 *   onTestFailure    → reuses the captured bytes for the Jenkins TestNG reporter
 *                       embed and saves a PNG file to test-output/screenshots/.
 */
public class TestListener implements ITestListener, IInvokedMethodListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    // Holds screenshot bytes captured in afterInvocation so onTestFailure can
    // reuse them without taking a second screenshot.
    private static final ThreadLocal<byte[]> FAILURE_SCREENSHOT = new ThreadLocal<>();

    // ---------------------------------------------------------------
    // IInvokedMethodListener — fires before ITestListener events
    // ---------------------------------------------------------------

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        // no-op
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (!method.isTestMethod()) return;
        if (result.getStatus() != ITestResult.FAILURE) return;
        if (!DriverManager.isDriverActive()) return;

        byte[] bytes = ScreenshotUtils.captureScreenshotAsBytes();
        FAILURE_SCREENSHOT.set(bytes);

        // Attach to Allure here — the AllureTestNg listener has not yet closed
        // the test case, so the attachment is correctly bound to this test.
        if (bytes.length > 0) {
            Allure.addAttachment(
                result.getMethod().getMethodName() + " - failure screenshot",
                "image/png", new ByteArrayInputStream(bytes), "png");
        }
    }

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

        // Reuse bytes already captured in afterInvocation (Allure attachment done there)
        byte[] bytes = FAILURE_SCREENSHOT.get();
        FAILURE_SCREENSHOT.remove();

        embedScreenshotInReporter(result, bytes);
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

    private void embedScreenshotInReporter(ITestResult result, byte[] bytes) {
        if (bytes == null || bytes.length == 0) return;
        try {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            Reporter.setCurrentTestResult(result);
            Reporter.log("<br/><b>Failure Screenshot:</b><br/>" +
                "<img src='data:image/png;base64," + base64 +
                "' style='max-width:900px;border:2px solid #e74c3c;'/><br/>", true);
        } catch (Exception e) {
            log.error("Could not embed screenshot in reporter: {}", e.getMessage());
        }
    }
}