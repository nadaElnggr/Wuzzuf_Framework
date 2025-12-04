# Selenium Reporting Framework

This repository contains an advanced **Selenium + TestNG** automation framework for testing the Wuzzuf site, with:

- Page Object Model (POM) structure
- Centralized configuration
- WebDriver management via WebDriverManager
- Allure reporting (screenshots, logs, history)
- Failure email notifications with inline screenshots
- Video recording support for test execution
- Simple data-driven capabilities (via external files)
- Utility PowerShell scripts for time-stamped Allure reports and cleanup

The goal is to make it **very easy for anyone to clone, configure, and run** the tests on their own machine.

---

## 1. Project Structure

Project root (important parts):

```text
Wuzzuf_Reporting/
  pom.xml
  testng.xml
  src/
    main/
      java/
        CoreFramework/
          actions/          # DriverFactory and actions helpers
          config/           # ConfigManager (config.properties loader)
          listeners/        # TestListener (Allure + email on failure)
          pages/            # Page Object classes (e.g., LoginPage)
          utils/
            reporting/      # ReportManager, EmailSender, StepLogger, VideoRecorder
    test/
      java/
        BaseTest/           # BaseTest (common setup/teardown)
        CoreTest/           # Test classes (e.g., LoginTest)
      resources/
        Artifacts/
          ScreenShots/      # Saved screenshots for Allure
          VideoRecords/     # Saved video recordings
        TestData/           # External test data (CSV/Excel/etc.)
        config.properties   # Environment & framework configuration
  allure-results/           # Allure JSON results (generated after tests)
  allure-history/           # Time-stamped HTML Allure reports (via script)
  run-tests-with-allure.ps1 # Script: run tests and generate time-stamped Allure report
  cleanup-allure.ps1        # Script: delete old Allure reports (retention)
```

---

## 2. Key Framework Components

### 2.1 BaseTest

Located at: `src/test/java/BaseTest/BaseTest.java`

Responsible for:

- Test setup (`@BeforeMethod`):
  - Initializes WebDriver via `DriverFactory.initDriver()`
  - Logs the base URL from `ConfigManager`
- Test teardown (`@AfterMethod`):
  - Logs PASSED / FAILED / SKIPPED status
  - Quits the WebDriver
- Registers listeners (via `@Listeners`, if used) so that Allure and custom `TestListener` run even when executing tests directly from IntelliJ.

### 2.2 DriverFactory

Located at: `src/main/java/CoreFramework/actions/DriverFactory.java`

Responsibilities:

- Maintains a **ThreadLocal<WebDriver>** so tests can run in parallel safely
- Reads `browser` from `ConfigManager.getBrowser()` and supports:
  - `chrome`
  - `firefox`
  - `edge`
- Uses **WebDriverManager** to resolve and download the correct browser drivers automatically
- Applies implicit waits and maximizes the window.

### 2.3 ConfigManager

Located at: `src/main/java/CoreFramework/config/ConfigManager.java`

- Loads configuration from `config.properties` under `src/test/resources`.
- Allows overrides via **system properties** (`-Dkey=value`).
- Typical keys:

  ```properties
  baseUrl=https://***.basharsys.com/jobs
  browser=chrome
  implicitWait=5
  explicitWait=10

  env=prod

  smtpUser=yourgmail@gmail.com
  smtpPassword=your_app_password
  notificationEmails=someone@company.com,another@company.com
  ```

- Provides helper getters like:
  - `getBaseUrl()`
  - `getBrowser()`
  - `getImplicitWait()`
  - `getExplicitWait()`
  - `getEnv()`
  - `getSmtpUser()`, `getSmtpPassword()`
  - `getNotificationEmails()`

### 2.4 TestListener

Located at: `src/main/java/CoreFramework/listeners/TestListener.java`

Implements `ITestListener` and is responsible for everything that happens **around** test execution:

- On test start:
  - Starts video recording (`VideoRecorder.startRecording(testName)`).
- On test success:
  - Stops and deletes the video (to save space).
- On test failure:
  - Attaches a screenshot and log to Allure via `ReportManager.attachFailureScreenshot(testName)`.
  - Captures screenshot bytes directly from WebDriver.
  - Fetches logged steps from `StepLogger`.
  - Builds an **HTML email** with:
    - Environment
    - Severity (based on Allure `@Severity` annotation if present)
    - Class name, test name, description
    - Ordered list of steps
    - Inline screenshot (`<img src="cid:screenshot">`).
  - Sends the email via `EmailSender.sendFailureEmailHtmlWithScreenshot(...)`.
  - Clears steps from `StepLogger`.

> ⚠️ For this to work when using IntelliJ "Run", make sure the listener is either:
> - Registered in `testng.xml`, or
> - Annotated on `BaseTest` with `@Listeners({AllureTestNg.class, TestListener.class})`.

### 2.5 ReportManager

Located at: `src/main/java/CoreFramework/utils/reporting/ReportManager.java`

- Manages saving screenshots and video references into Allure.
- Screenshot flow:
  - Takes a screenshot via WebDriver.
  - Saves `.png` into `src/test/resources/Artifacts/ScreenShots`.
  - Exposes `@Attachment`-annotated methods so Allure adds the screenshot to the report.
- Also manages attaching execution logs and video files (if using video recording).

### 2.6 EmailSender

Located at: `src/main/java/CoreFramework/utils/reporting/EmailSender.java`

- Uses **Jakarta Mail** (`jakarta.mail` and `jakarta.activation`) to send HTML emails.
- Only sends email if:
  - `env` is `prod` (`ConfigManager.isProd()`), unless you remove that guard.
  - `notificationEmails` is non-empty.
  - `smtpUser` and `smtpPassword` are configured.
- Builds a `MimeMessage` with:
  - `multipart/related` content
  - HTML body
  - Optional inline screenshot part, using `Content-ID: <screenshot>` to match the HTML `<img src="cid:screenshot">`.

---

## 3. Dependencies & Tools Required

### 3.1 Downloads & Installations

To run this framework, you need:

1. **Java JDK 17+**
   - Download from: OpenJDK / Oracle JDK
   - Verify:
     ```bash
     java -version
     ```

2. **Maven 3.8+**
   - Download and add to `PATH`
   - Verify:
     ```bash
     mvn -version
     ```

3. **Allure Commandline**
   - Install via:
     - Scoop / Chocolatey on Windows, or
     - Direct ZIP from Allure site
   - Verify:
     ```bash
     allure --version
     ```

4. **An IDE** (recommended):
   - IntelliJ IDEA (Ultimate or Community)
   - Import project as a **Maven** project.

5. **Browsers:**
   - Chrome (and/or Firefox, Edge) depending on which browsers you want to run tests on.

### 3.2 Maven Dependencies (from `pom.xml`)

Key dependencies are already defined:

- `org.seleniumhq.selenium:selenium-java`
- `org.testng:testng`
- `io.github.bonigarcia:webdrivermanager`
- `io.qameta.allure:allure-testng`
- `org.slf4j:slf4j-api`
- `ch.qos.logback:logback-classic`
- `org.aspectj:aspectjweaver`
- `com.github.stephenc.monte:monte-screen-recorder`
- `com.sun.mail:jakarta.mail`
- `jakarta.activation:jakarta.activation-api`

You usually don’t need to do anything extra: Maven will download them on first build.

---

## 4. Configuration

The main configuration file is:

```text
src/test/resources/config.properties
```

Example content:

```properties
baseUrl=https://****.com/jobs
browser=chrome
implicitWait=5
explicitWait=10

# Environment
env=prod

# SMTP & email notifications
smtpUser=yourgmail@gmail.com
smtpPassword=your_app_password
notificationEmails=tester1@company.com,tester2@company.com
```

You can override any property with system properties:

```bash
mvn clean test -Dbrowser=firefox -Denv=testing
```

---

## 5. How to Run Tests

### 5.1 Using Maven (recommended)

From project root:

```bash
mvn clean test
```

This will:

- Run all `*Test.java` classes using `testng.xml` as the suite.
- Generate Allure **results** (`JSON`) into `allure-results` folder (or configured path).

### 5.2 Run a specific test class from IntelliJ

- Open `LoginTest.java` under `src/test/java/CoreTest`.
- Right-click → **Run 'LoginTest'**.
- Ensure:
  - Listeners are registered either in `testng.xml` or via `@Listeners` on `BaseTest`.

### 5.3 Change browser

Either edit `config.properties`:

```properties
browser=firefox
```

or use a system property:

```bash
mvn clean test -Dbrowser=firefox
```

---

## 6. Allure Reporting

### 6.1 Generate Allure Report Manually

If Allure results are in `allure-results` at project root:

```bash
allure generate allure-results -o allure-report --clean
```

Then open:

```text
allure-report/index.html
```

### 6.2 Time-stamped Allure History (PowerShell scripts)

Two scripts in project root:

1. `run-tests-with-allure.ps1`  
   - Runs:
     - `mvn clean test`
     - `allure generate allure-results -o allure-history/<timestamp> --clean`
   - Usage:

     ```powershell
     powershell -ExecutionPolicy Bypass -File .
un-tests-with-allure.ps1
     ```

   - Each run creates a folder like:

     ```text
     allure-history/
       2025-11-30_090922/
         index.html
         history/
         ...
     ```

2. `cleanup-allure.ps1`  
   - Deletes **old** timestamped report folders (e.g., older than 90 days).
   - Usage:

     ```powershell
     powershell -ExecutionPolicy Bypass -File .\cleanup-allure.ps1
     ```

   - You can schedule this via Windows Task Scheduler, or run manually.

This combination gives you **3 months of Allure history** while keeping disk usage under control.

---

## 7. Email Notifications on Failure

To enable email alerts when a test fails:

1. Set `env=prod` in `config.properties` (or adjust `EmailSender` to send in all envs).
2. Configure Gmail (or other) SMTP:

   ```properties
   smtpUser=yourgmail@gmail.com
   smtpPassword=your_app_password   # Use App Password if using Gmail + 2FA
   notificationEmails=qa1@company.com,lead@company.com
   ```

3. On failure, the framework will:
   - Capture a screenshot from WebDriver.
   - Build an HTML email with:
     - Test details
     - Steps (from `StepLogger`)
     - Inline screenshot.
   - Send the email to `notificationEmails`.

If something is wrong, check console for logs starting with `[EmailSender]`.

---

## 8. How to Extend the Framework

### 8.1 Add a new Page

1. Create a new class under `src/main/java/CoreFramework/pages`, e.g. `SearchPage.java`.
2. Extend `BasePage` (if existing) and add WebElements with `@FindBy`.
3. Initialize via `PageFactory.initElements(driver, this);` in the constructor.
4. Add business methods like `searchForJob`, `openJobDetails`, etc.

### 8.2 Add a new Test

1. Create a test class under `src/test/java/CoreTest`, e.g. `SearchTest.java`.
2. Extend `BaseTest`.
3. Use your page objects and `StepLogger`:

   ```java
   @Test(description = "Verify search functionality with valid keywords")
   @Severity(SeverityLevel.CRITICAL)
   public void searchForJobShouldReturnResults() {
       StepLogger.log("Open base URL");
       driver.get(ConfigManager.getBaseUrl());

       StepLogger.log("Search for 'QA Engineer'");
       searchPage.search("QA Engineer");

       StepLogger.log("Verify results displayed");
       searchPage.assertResultsVisible();
   }
   ```

On failure, these steps will appear both in Allure and in the email body.

---

## 9. Troubleshooting

- **No Allure attachments (screenshots) visible:**
  - Ensure `TestListener` is registered (via `@Listeners` or `testng.xml`).
  - Check if WebDriver is still alive when failure happens.

- **Email not sent:**
  - Check console for `[EmailSender]` messages.
  - Confirm `env=prod`, `smtpUser`, `smtpPassword`, and `notificationEmails` are set.
  - For Gmail, make sure you’re using an **App Password** and that security settings allow SMTP.

- **No `allure-results` folder after tests:**
  - Ensure `allure-testng` dependency is on the classpath.
  - Make sure you are running via TestNG, not JUnit.
  - Check that Surefire isn’t misconfigured.

---

Happy testing 🚀  
This framework is ready for team use: just configure `config.properties`, run `mvn clean test`, and use the PowerShell scripts for rich Allure history and automatic cleanup.
