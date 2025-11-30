package CoreFramework.utils.reporting;

import CoreFramework.actions.DriverFactory;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.*;

/**
 * Central place for Allure reporting:
 *  - Screenshots
 *  - Execution log
 *  - Text / JSON attachments
 *  - Video attachments (when a video file already exists)
 *
 * This class does NOT record video itself.
 * It only reads existing files and attaches them to Allure.
 */
public class ReportManager {

    private static final Path ARTIFACTS_BASE =  Paths.get("src", "test", "resources", "Artifacts");
    private static final Path SCREENSHOTS_DIR = ARTIFACTS_BASE.resolve("ScreenShots");
    private static final Path VIDEOS_DIR      = ARTIFACTS_BASE.resolve("VideoRecords");

    static {
        try {
            Files.createDirectories(SCREENSHOTS_DIR);
            Files.createDirectories(VIDEOS_DIR);
        } catch (Exception ignored) {
        }
    }

    // ======================== PUBLIC API ========================

    /**
     * Attach common failure artifacts:
     *  - Screenshot
     *  - Execution log
     */
    public static void attachFailureScreenshot(String testName) {
        attachScreenshot("Failure - " + testName, testName);
//        attachExecutionLog();
    }

    /**
     * Attach common failure artifacts:
     *  - Screenshot
     *  - Execution log
     *  - Video (if videoFilePath is not null/empty and exists)
     */
    public static void attachFailureVideoRecord(String testName, String videoFilePath) {
        attachScreenshot("Failure - " + testName, testName);
//        attachExecutionLog();
        if (videoFilePath != null && !videoFilePath.isEmpty()) {
            attachVideo("Video - " + testName, videoFilePath);
        }
    }

    /**
     * Take and attach a screenshot for the given test name.
     * The screenshot file is saved under:
     *  target/artifacts/screenshots/<testName>_<timestamp>.png
     */
    public static void attachScreenshot(String displayName, String testName) {
        Path path = takeScreenshotToFile(testName);
        if (path != null) {
            attachScreenshotInternal(displayName, path);
        }
    }

    /**
     * Attach a text message as a plain text attachment.
     */
    public static void logInfo(String name, String message) {
        attachText(name, message);
    }

    /**
     * Attach a JSON string as an application/json attachment.
     */
    public static void attachJson(String name, String json) {
        attachJsonInternal(name, json);
    }

    /**
     * Attach an existing video file (MP4) to Allure.
     * The file must already exist at the provided path.
     */
    public static void attachVideo(String name, String videoFilePath) {
        attachVideoInternal(name, videoFilePath);
    }

    // ================= INTERNAL IMPLEMENTATION =================

    /**
     * Take a screenshot with WebDriver and save it to a file
     * under the screenshots directory.
     */
    private static Path takeScreenshotToFile(String testName) {
        WebDriver driver;
        try {
            driver = DriverFactory.getDriver();
        } catch (IllegalStateException ex) {
            // Driver is not initialized
            return null;
        }

        if (driver == null) {
            return null;
        }

        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String fileName = testName + "_" + System.currentTimeMillis() + ".png";
            Path dest = SCREENSHOTS_DIR.resolve(fileName);
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest;
        } catch (Exception e) {
            return null;
        }
    }

    @Attachment(value = "{name}", type = "image/png")
    private static byte[] attachScreenshotInternal(String name, Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Attachment(value = "{attachName}", type = "text/plain")
    private static byte[] attachText(String attachName, String message) {
        if (message == null) {
            return new byte[0];
        }
        return message.getBytes();
    }

    @Attachment(value = "{attachName}", type = "application/json")
    private static byte[] attachJsonInternal(String attachName, String json) {
        if (json == null) {
            return new byte[0];
        }
        return json.getBytes();
    }

    /**
     * Attach the execution log file as text.
     * This assumes Logback (or your logger) is writing to:
     *  target/logs/test.log
     */
//    @Attachment(value = "Execution log", type = "text/plain")
//    private static byte[] attachExecutionLog() {
//        try {
//            Path path = Paths.get("target/logs/test.log");
//            if (Files.exists(path)) {
//                return Files.readAllBytes(path);
//            }
//        } catch (Exception ignored) {
//        }
//        return new byte[0];
//    }

    @Attachment(value = "{name}", type = "video/mp4")
    private static byte[] attachVideoInternal(String name, String videoFilePath) {
        try {
            Path path = Paths.get(videoFilePath);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            }
        } catch (Exception ignored) {
        }
        return new byte[0];
    }
}


