package BaseTest;

import CoreFramework.config.ConfigManager;
import CoreFramework.actions.DriverFactory;
import CoreFramework.listeners.TestListener;
import io.qameta.allure.testng.AllureTestNg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners({AllureTestNg.class, TestListener.class})
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        log.info("===== Test setup started =====");
        DriverFactory.initDriver();
        log.info("WebDriver initialized");
        // Optionally go to base URL:
         DriverFactory.getDriver().get(ConfigManager.getBaseUrl());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            String testName = result.getMethod().getMethodName();

            if (result.isSuccess()) {
                log.info("===== Test '{}' PASSED =====", testName);
            } else {
                log.error("===== Test '{}' FAILED =====", testName, result.getThrowable());
//                ScreenshotUtils.takeScreenshot("Failure - " + testName);
            }
        } finally {
            log.info("Quitting WebDriver");
            DriverFactory.quitDriver();
            log.info("===== Test teardown finished =====");
        }
    }

    protected String getBaseUrl() {
        return ConfigManager.getBaseUrl();
    }

}
