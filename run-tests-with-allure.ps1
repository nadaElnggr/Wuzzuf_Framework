# ================================
# run-tests-with-allure.ps1
# ================================
# 1) Run `mvn clean test` (even if tests fail, continue)
# 2) Generate an Allure report
# 3) Save the report into a timestamped folder
#    e.g. <project>\allure-history\2025-11-30_083012\index.html
# ================================

# 1) Detect project root (folder where this script lives)
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# 2) Where Allure results (JSON) are stored
#    👉 In your case: root\allure-results
$AllureResults = Join-Path $ProjectRoot "allure-results"

# 3) Root folder where you want to store ALL timestamped reports
#    👉 In your case: root\allure-history
$ReportsRoot = Join-Path $ProjectRoot "allure-history"

# Create reports root if it doesn't exist
if (-not (Test-Path $ReportsRoot)) {
    New-Item -ItemType Directory -Path $ReportsRoot | Out-Null
}

# 4) Build timestamped folder name (e.g. "2025-11-30_083012")
$Timestamp = Get-Date -Format "yyyy-MM-dd_HHmmss"
$CurrentReportDir = Join-Path $ReportsRoot $Timestamp

Write-Host "Project root     : $ProjectRoot"
Write-Host "Allure results   : $AllureResults"
Write-Host "Reports root     : $ReportsRoot"
Write-Host "This run report  : $CurrentReportDir"
Write-Host ""

# 5) Run tests with Maven
Write-Host "===== Running: mvn clean test ====="
Set-Location $ProjectRoot
mvn clean test
$testExitCode = $LASTEXITCODE

Write-Host ""
Write-Host "mvn clean test finished with exit code: $testExitCode"
Write-Host "(We will still try to generate Allure report if results exist.)"

# 6) Check that allure-results exists
if (-not (Test-Path $AllureResults)) {
    Write-Host ""
    Write-Host "❌ No Allure results found at: $AllureResults"
    Write-Host "Make sure your tests are writing results to this folder."
    exit 1
}

# 7) Generate Allure report into the timestamped folder
Write-Host ""
Write-Host "===== Generating Allure report ====="
Write-Host "allure generate `"$AllureResults`" -o `"$CurrentReportDir`" --clean"
allure generate "$AllureResults" -o "$CurrentReportDir" --clean
$allureExitCode = $LASTEXITCODE

if ($allureExitCode -ne 0) {
    Write-Host ""
    Write-Host "❌ Failed to generate Allure report (exit code $allureExitCode)."
    Write-Host "Check that Allure CLI is installed and on your PATH (run 'allure --version')."
    exit $allureExitCode
}

Write-Host ""
Write-Host "✅ Allure report generated at:"
Write-Host "   $CurrentReportDir"
Write-Host ""
Write-Host "Open this file in your browser:"
Write-Host "   $($CurrentReportDir)\index.html"

# Return overall exit code (tests may have failed)
exit $testExitCode
