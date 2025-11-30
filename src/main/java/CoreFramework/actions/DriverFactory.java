package CoreFramework.actions;

import CoreFramework.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static void initDriver() {
        if (driver.get() == null) {
            String browser = ConfigManager.getBrowser().toLowerCase();

            WebDriver webDriver;

            switch (browser) {
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions ffOptions = new FirefoxOptions();
                    webDriver = new FirefoxDriver(ffOptions);
                    break;
                case "edge":
                    WebDriverManager.edgedriver().setup();
                    EdgeOptions edgeOptions = new EdgeOptions();
                    webDriver = new EdgeDriver(edgeOptions);
                    break;
                case "chrome":
                default:
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions chOptions = new ChromeOptions();
                    webDriver = new ChromeDriver(chOptions);
                    break;
            }

            webDriver.manage().window().maximize();
            webDriver.manage().timeouts()
                    .implicitlyWait(Duration.ofSeconds(ConfigManager.getImplicitWait()));

            driver.set(webDriver);
        }
    }

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            throw new IllegalStateException("WebDriver is not initialized. " +
                    "Call DriverFactory.initDriver() first.");
        }
        return driver.get();
    }

    public static void quitDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            webDriver.quit();
            driver.remove();
        }
        else {
            throw new IllegalStateException("WebDriver has not been initialized for this thread.");

        }
    }
}
