package CoreFramework.listeners;

import CoreFramework.utils.VideoRecorder;
import CoreFramework.utils.reporting.EmailSender;
import CoreFramework.utils.reporting.StepLogger;
import CoreFramework.config.ConfigManager;
import  CoreFramework.utils.reporting.ReportManager;

import java.lang.reflect.Method;
import java.util.List;
import CoreFramework.actions.DriverFactory;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;


import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        VideoRecorder.startRecording(testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String videoPath = VideoRecorder.stopRecording();
        // Delete videos for passed tests to save space
        VideoRecorder.deleteIfExists(videoPath);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName  = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        String description = result.getMethod().getDescription();
        String env = ConfigManager.getEnv();
        String severity = getSeverity(result);

        //Allure pic attachement
        ReportManager.attachFailureScreenshot(testName);

//        //Allure Video Record
//        String videoPath = VideoRecorder.stopRecording();
//        ReportManager.attachFailureVideoRecord(testName, videoPath);


        // 1) Capture screenshot bytes (this will be used by EmailSender)
        byte[] screenshotBytes = captureScreenshotBytes();

        // 2) Get steps from StepLogger
        List<String> steps = StepLogger.getSteps();

        // 3) Build HTML body with <img src="cid:screenshot">
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: Arial, sans-serif;\">");

        html.append("<h2 style=\"color:#c0392b;\">Automated test FAILED</h2>");

        html.append("<p>");
        html.append("<b>Environment:</b> ").append(env).append("<br/>");
        html.append("<b>Severity:</b> ").append(escapeHtml(severity)).append("<br/>");
        html.append("<b>Class:</b> ").append(className).append("<br/>");
        html.append("<b>Test:</b> ").append(testName).append("</p>");

        if (description != null && !description.isBlank()) {
            html.append("<p><b>Description:</b> ")
                    .append(escapeHtml(description))
                    .append("</p>");
        }

        html.append("<h3>Steps</h3>");
        if (steps == null || steps.isEmpty()) {
            html.append("<p><i>No steps were logged.</i></p>");
        } else {
            html.append("<ol>");
            for (String step : steps) {
                html.append("<li>").append(escapeHtml(step)).append("</li>");
            }
            html.append("</ol>");
        }

        html.append("<h3>Screenshot</h3>");
        if (screenshotBytes != null && screenshotBytes.length > 0) {
            html.append("<p>The screenshot at the moment of failure:</p>");
            // IMPORTANT: this cid "screenshot" must match Content-ID in EmailSender
            html.append("<img src=\"cid:screenshot\" style=\"max-width:900px;border:1px solid #ccc;\"/>");
        } else {
            html.append("<p><i>Screenshot not available.</i></p>");
        }

        html.append("<p style=\"margin-top:20px;\">Best regards,<br/>Automation Framework</p>");
        html.append("</body></html>");

        // 4) Send email with screenshot
        String[] recipients = ConfigManager.getNotificationEmails();
        String subject = "[Automation Failure][" + severity + "] "
                + testName+ " is not working on production";

        EmailSender.sendFailureEmailHtmlWithScreenshot(
                recipients,
                subject,
                html.toString(),
                screenshotBytes
        );

        StepLogger.clear();
    }



    private byte[] captureScreenshotBytes() {
        try {
            WebDriver driver = DriverFactory.getDriver();
            if (driver == null) {
                System.out.println("[TestListener] WebDriver is null, cannot capture screenshot.");
                return null;
            }

            if (!(driver instanceof TakesScreenshot)) {
                System.out.println("[TestListener] WebDriver does not support TakesScreenshot.");
                return null;
            }

            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            System.out.println("[TestListener] Failed to capture screenshot: " + e.getMessage());
            return null;
        }
    }

    private String getSeverity(ITestResult result) {
        try {
            Method method = result.getMethod()
                    .getConstructorOrMethod()
                    .getMethod();

            Severity severityAnn = method.getAnnotation(Severity.class);
            if (severityAnn != null && severityAnn.value() != null) {
                SeverityLevel level = severityAnn.value();
                return level.name(); // e.g. "CRITICAL"
            }
        } catch (Exception e) {
            System.out.println("[TestListener] Failed to read @Severity: " + e.getMessage());
        }
        return "UNSPECIFIED";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }


}


//    @Override
//    public void onTestSkipped(ITestResult result) {
//        String videoPath = VideoRecorder.stopRecording();
//        VideoRecorder.deleteIfExists(videoPath);
//    }

   

