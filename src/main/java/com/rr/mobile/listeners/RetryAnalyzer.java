package com.rr.mobile.listeners;

import com.rr.mobile.config.FrameworkConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test up to {@link FrameworkConfig#MAX_RETRY_COUNT} times.
 *
 * Attach per-test via:  @Test(retryAnalyzer = RetryAnalyzer.class)
 * or register globally through the TestNG listener / annotationTransformer.
 *
 * Each test method gets its own RetryAnalyzer instance (TestNG guarantees this),
 * so the per-instance counter is safe in parallel execution.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);

    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < FrameworkConfig.MAX_RETRY_COUNT) {
            retryCount++;
            log.warn("[RETRY] '{}' – attempt {}/{} after: {}",
                result.getMethod().getMethodName(),
                retryCount,
                FrameworkConfig.MAX_RETRY_COUNT,
                result.getThrowable() != null ? result.getThrowable().getMessage() : "unknown error");
            return true;
        }
        log.error("[RETRY EXHAUSTED] '{}' failed after {} attempts.",
            result.getMethod().getMethodName(), FrameworkConfig.MAX_RETRY_COUNT);
        return false;
    }
}
