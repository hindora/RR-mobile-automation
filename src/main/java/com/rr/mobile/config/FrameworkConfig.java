package com.rr.mobile.config;

/**
 * Central repository of framework-wide constants.
 * All tuneable values (timeouts, paths, retry counts) live here
 * so they can be changed in one place without hunting through code.
 */
public final class FrameworkConfig {

    // ---------------------------------------------------------------
    // File paths
    // ---------------------------------------------------------------
    public static final String CONFIG_FILE_PATH   = "src/test/resources/config/config.properties";
    public static final String TEST_DATA_DIR      = "src/test/resources/testdata/";
    public static final String SCREENSHOT_DIR     = "test-output/screenshots/";
    public static final String LOG_DIR            = "test-output/logs/";

    // ---------------------------------------------------------------
    // Wait timeouts (seconds)
    // ---------------------------------------------------------------
    public static final int DEFAULT_EXPLICIT_WAIT  = 15;
    public static final int DEFAULT_IMPLICIT_WAIT  = 10;
    public static final int FLUENT_WAIT_POLLING    = 2;
    public static final int PAGE_LOAD_TIMEOUT      = 30;

    // ---------------------------------------------------------------
    // Retry
    // ---------------------------------------------------------------
    public static final int MAX_RETRY_COUNT = 0;

    // ---------------------------------------------------------------
    // Gesture defaults
    // ---------------------------------------------------------------
    public static final long SWIPE_DURATION_MS     = 800;
    public static final long LONG_PRESS_DURATION_MS = 2000;

    private FrameworkConfig() {
        // utility class – no instantiation
    }
}
