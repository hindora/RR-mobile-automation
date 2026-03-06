package com.rr.mobile.driver;

import com.rr.mobile.config.ConfigReader;
import com.rr.mobile.config.FrameworkConfig;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Responsible only for constructing a fully-configured AndroidDriver.
 * Does NOT own the driver lifecycle – that is BaseTest's responsibility.
 *
 * Capabilities are read from ConfigReader so that nothing is hard-coded
 * here; CI pipelines can override values via system properties.
 */
public final class DriverFactory {

    private static final Logger log = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {}

    /**
     * Create an AndroidDriver connected to a local physical device.
     * The caller is responsible for calling {@code driver.quit()} when done.
     */
    public static AndroidDriver createAndroidDriver() {
        ConfigReader config = ConfigReader.getInstance();
        UiAutomator2Options options = buildCapabilities(config);

        try {
            URL serverUrl = new URL(config.getAppiumServerUrl());
            log.info("Creating AndroidDriver → server: {}", serverUrl);

            AndroidDriver driver = new AndroidDriver(serverUrl, options);

            // Implicit wait – acts as a baseline; explicit waits override per-call
            driver.manage().timeouts()
                  .implicitlyWait(Duration.ofSeconds(FrameworkConfig.DEFAULT_IMPLICIT_WAIT));

            log.info("AndroidDriver created. Session: {}", driver.getSessionId());
            return driver;

        } catch (MalformedURLException e) {
            throw new RuntimeException(
                "Invalid Appium server URL: " + config.getAppiumServerUrl(), e);
        }
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private static UiAutomator2Options buildCapabilities(ConfigReader config) {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setDeviceName(config.getDeviceName());
        options.setPlatformVersion(config.getPlatformVersion());
        options.setUdid(config.getUdid());
        options.setNewCommandTimeout(Duration.ofSeconds(config.getNewCommandTimeout()));
        options.setAutoGrantPermissions(config.isAutoGrantPermissions());
        options.setNoReset(config.isNoReset());
        options.setFullReset(config.isFullReset());

        // App launch strategy
        String appPath = config.getAppPath();
        if (!appPath.isBlank()) {
            options.setApp(appPath);
            log.info("App capability set to APK path: {}", appPath);
        } else {
            // Launch an already-installed app
            options.setAppPackage(config.getAppPackage());
            options.setAppActivity(config.getAppActivity());
            log.info("App capability set to package/activity: {}/{}",
                     config.getAppPackage(), config.getAppActivity());
        }

        log.info("UiAutomator2Options → deviceName={}, udid={}, platformVersion={}",
                 config.getDeviceName(), config.getUdid(), config.getPlatformVersion());
        return options;
    }
}
