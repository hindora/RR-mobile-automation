package com.rr.mobile.utils;

import com.rr.mobile.config.FrameworkConfig;
import com.rr.mobile.driver.DriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Static utility for capturing screenshots.
 * Called by TestListener on failure and by tests on demand.
 */
public final class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {}

    /**
     * Captures a screenshot and saves it to {@code test-output/screenshots/<testName>_<timestamp>.png}.
     *
     * @param testName used as the filename prefix
     * @return absolute path of the saved file, or {@code null} on failure
     */
    public static String captureScreenshot(String testName) {
        ensureDirectoryExists();
        String filename = testName + "_" + LocalDateTime.now().format(TS) + ".png";
        String fullPath = FrameworkConfig.SCREENSHOT_DIR + filename;
        try {
            File src  = ((TakesScreenshot) DriverManager.getDriver())
                            .getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(src, new File(fullPath));
            log.info("Screenshot saved: {}", fullPath);
            return new File(fullPath).getAbsolutePath();
        } catch (IOException e) {
            log.error("Failed to write screenshot file: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Captures a screenshot and returns it as a raw byte array.
     * Used by TestListener to embed images directly in the ExtentReport.
     *
     * @return PNG bytes, or an empty array on failure
     */
    public static byte[] captureScreenshotAsBytes() {
        try {
            return ((TakesScreenshot) DriverManager.getDriver())
                       .getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.error("Failed to capture screenshot bytes: {}", e.getMessage());
            return new byte[0];
        }
    }

    /** Creates the screenshot output directory if it does not already exist. */
    public static void ensureDirectoryExists() {
        File dir = new File(FrameworkConfig.SCREENSHOT_DIR);
        if (!dir.exists() && dir.mkdirs()) {
            log.debug("Screenshot directory created: {}", dir.getAbsolutePath());
        }
    }
}
