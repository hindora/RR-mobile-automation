package com.rr.mobile.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Thread-safe singleton that loads config.properties once and exposes
 * typed accessors for every capability used by DriverFactory.
 *
 * System properties always override file values, enabling CI overrides:
 *   mvn test -Ddevice.udid=emulator-5554 -Dplatform.version=14
 */
public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);

    private static volatile ConfigReader instance;
    private final Properties properties = new Properties();

    private ConfigReader() {
        loadProperties();
    }

    public static ConfigReader getInstance() {
        if (instance == null) {
            synchronized (ConfigReader.class) {
                if (instance == null) {
                    instance = new ConfigReader();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream in = new FileInputStream(FrameworkConfig.CONFIG_FILE_PATH)) {
            properties.load(in);
            log.info("Config loaded from: {}", FrameworkConfig.CONFIG_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(
                "Cannot load configuration file: " + FrameworkConfig.CONFIG_FILE_PATH, e);
        }
    }

    // ---------------------------------------------------------------
    // Appium server
    // ---------------------------------------------------------------
    public String getAppiumServerUrl() {
        return get("appium.server.url");
    }

    // ---------------------------------------------------------------
    // Device
    // ---------------------------------------------------------------
    public String getDeviceName() {
        return get("device.name");
    }

    public String getPlatformVersion() {
        return get("platform.version");
    }

    public String getUdid() {
        return get("device.udid");
    }

    // ---------------------------------------------------------------
    // App
    // ---------------------------------------------------------------
    public String getAppPath() {
        return getOrDefault("app.path", "");
    }

    public String getAppPackage() {
        return getOrDefault("app.package", "");
    }

    public String getAppActivity() {
        return getOrDefault("app.activity", "");
    }

    // ---------------------------------------------------------------
    // Behaviour flags
    // ---------------------------------------------------------------
    public boolean isAutoGrantPermissions() {
        return Boolean.parseBoolean(getOrDefault("auto.grant.permissions", "true"));
    }

    public boolean isNoReset() {
        return Boolean.parseBoolean(getOrDefault("no.reset", "false"));
    }

    public boolean isFullReset() {
        return Boolean.parseBoolean(getOrDefault("full.reset", "false"));
    }

    public int getNewCommandTimeout() {
        return Integer.parseInt(getOrDefault("new.command.timeout", "60"));
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    /** Required property – throws if missing. */
    private String get(String key) {
        // System property takes precedence (CI override)
        String value = System.getProperty(key, properties.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Required property '" + key + "' is not set.");
        }
        return value.trim();
    }

    /** Optional property with fallback default. */
    private String getOrDefault(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue)).trim();
    }
}
