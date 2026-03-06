package com.rr.mobile.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.rr.mobile.config.FrameworkConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages a single ExtentReports instance (created once per suite run) and
 * per-thread ExtentTest instances so parallel tests each get their own node
 * in the report.
 */
public final class ExtentReportManager {

    private static final Logger log = LogManager.getLogger(ExtentReportManager.class);

    private static volatile ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> TEST_THREAD_LOCAL = new ThreadLocal<>();

    private ExtentReportManager() {}

    // ---------------------------------------------------------------
    // Suite-level lifecycle
    // ---------------------------------------------------------------

    /** Returns (or lazily creates) the shared ExtentReports instance. */
    public static ExtentReports getInstance() {
        if (extentReports == null) {
            synchronized (ExtentReportManager.class) {
                if (extentReports == null) {
                    extentReports = createReport();
                }
            }
        }
        return extentReports;
    }

    /** Must be called in {@code @AfterSuite} / {@code onFinish(ITestContext)}. */
    public static synchronized void flushReport() {
        if (extentReports != null) {
            extentReports.flush();
            log.info("ExtentReport flushed.");
        }
    }

    // ---------------------------------------------------------------
    // Test-level lifecycle
    // ---------------------------------------------------------------

    /** Creates an ExtentTest for the current thread. */
    public static ExtentTest createTest(String testName) {
        ExtentTest test = getInstance().createTest(testName);
        TEST_THREAD_LOCAL.set(test);
        return test;
    }

    public static ExtentTest createTest(String testName, String description) {
        ExtentTest test = (description != null && !description.isBlank())
            ? getInstance().createTest(testName, description)
            : getInstance().createTest(testName);
        TEST_THREAD_LOCAL.set(test);
        return test;
    }

    /** Returns the ExtentTest bound to the current thread (may be {@code null}). */
    public static ExtentTest getTest() {
        return TEST_THREAD_LOCAL.get();
    }

    /** Removes the ExtentTest reference for the current thread. */
    public static void removeTest() {
        TEST_THREAD_LOCAL.remove();
    }

    // ---------------------------------------------------------------
    // Private factory
    // ---------------------------------------------------------------

    private static ExtentReports createReport() {
        String timestamp  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportPath = FrameworkConfig.REPORT_DIR + "ExtentReport_" + timestamp + ".html";

        // Ensure directory exists
        new File(FrameworkConfig.REPORT_DIR).mkdirs();

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("RR Mobile Automation");
        spark.config().setReportName("Android Test Execution Report");
        spark.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(spark);
        reports.setSystemInfo("OS",             System.getProperty("os.name"));
        reports.setSystemInfo("Java Version",   System.getProperty("java.version"));
        reports.setSystemInfo("Framework",      "Appium 2.x + Java + TestNG");
        reports.setSystemInfo("Generated",      LocalDateTime.now().toString());

        log.info("ExtentReport initialised → {}", reportPath);
        return reports;
    }
}
