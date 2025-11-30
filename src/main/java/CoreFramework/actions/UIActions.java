package CoreFramework.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UIActions {
    private final Logger logger = Logger.getLogger(UIActions.class.getName());
    private final WebDriver driver;

    public UIActions() {
        // use the same driver manager used by BaseTest/BasePage/ReportManager
        this.driver = DriverFactory.getDriver();
        if (this.driver == null) {
            throw new IllegalStateException(
                    "WebDriver is not initialized. " +
                            "Make sure DriverFactory.initDriver() is called before using UIActions."
            );
        }
    }


public void navigateToPage(String url) {
        try {
            driver.navigate().to(url);
            logger.info("Navigated to page: " + url);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to navigate to page: " + url, e);
            throw e;
        }
    }


    public void click(SelectorType selector, String locator) throws InterruptedException {
        try {
            Thread.sleep(3000);
            WebElement element = findElement(selector, locator);
            element.click();
            logger.info("Clicked element: " + locator);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to click element: " + locator, e);
            throw e;
        }
    }


    public void setText(SelectorType selector, String locator, String text) throws InterruptedException {
        try {
            Thread.sleep(3000);
            WebElement element = findElement(selector, locator);
            element.sendKeys(text);
            logger.info("Sent keys"+text+" to element ");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send keys to element: " + locator, e);
            throw e;
        }
    }

    public void clearText(SelectorType selector, String locator) {
        try {
            WebElement element = findElement(selector, locator);
            element.clear();
            logger.info("Cleared text " );
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to clear text in element: " + locator,  e);
            throw e;
        }
    }
    public String getText(SelectorType selector, String locator) throws InterruptedException {
        try {
            Thread.sleep(3000);
            WebElement element = findElement(selector, locator);
            logger.info("Get text from element ");
            return element.getText();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get text from element: " + locator, e);
            throw e;
        }
    }
    public WebElement findElement(SelectorType selector, String locator) {
        return driver.findElement(selectElement(selector, locator));
    }


    public By selectElement(SelectorType selector,  String locator) {
        switch (selector) {
            case id : return By.id(locator);
            case cssSelector: return By.cssSelector(locator);
            case xpath: return By.xpath(locator);
            case tagname: return By.tagName(locator);
            case linktext: return By.linkText(locator);
            case classname: return By.className(locator);
            case name: return By.name(locator);
            case partiallinktext: return By.partialLinkText(locator);
            default: throw new IllegalArgumentException("Unsupported SelectorType: " + locator);

        }
    }
    public enum SelectorType{
        id,
        name,
        xpath,
        tagname,
        linktext,
        partiallinktext,
        classname,
        cssSelector
    }
}
