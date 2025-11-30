# ================================
# cleanup-allure.ps1
# ================================
# Deletes timestamped Allure report folders older than N days,
# based on the folder name pattern:
#   - yyyy-MM-dd_HHmmss  (e.g. 2025-11-30_090922)
#   - yyyy-M-d_HHmmss    (e.g. 2025-6-30_085300)
# ================================

# 1) Detect project root
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# 2) Root where your timestamped reports live
$ReportsRoot = Join-Path $ProjectRoot "allure-history"

# 3) How many days to keep
$DaysToKeep = 90
$thresholdDate = (Get-Date).AddDays(-$DaysToKeep)

Write-Host "Project root          : $ProjectRoot"
Write-Host "Reports root (history): $ReportsRoot"
Write-Host "Keeping last          : $DaysToKeep days (folders newer than $thresholdDate)"
Write-Host ""

if (-not (Test-Path $ReportsRoot)) {
    Write-Host "No allure-history folder found. Nothing to clean."
    exit 0
}

# Culture for parsing
$culture = [System.Globalization.CultureInfo]::InvariantCulture

# 4) Go through each subfolder in allure-history
Get-ChildItem -Path $ReportsRoot -Directory | ForEach-Object {
    $folder = $_
    $name = $folder.Name

    $parsedDate = $null

    # Try strict format yyyy-MM-dd_HHmmss first
    try {
        $parsedDate = [datetime]::ParseExact($name, "yyyy-MM-dd_HHmmss", $culture)
    }
    catch {
        # Try a more permissive format yyyy-M-d_HHmmss (handles 2025-6-30_...)
        try {
            $parsedDate = [datetime]::ParseExact($name, "yyyy-M-d_HHmmss", $culture)
        }
        catch {
            Write-Host "Skipping folder (name does not match expected date patterns): $name"
            return
        }
    }

    if ($parsedDate -lt $thresholdDate) {
        Write-Host "Deleting old report folder: $name (date=$parsedDate, threshold=$thresholdDate)"
        Remove-Item -Path $folder.FullName -Recurse -Force
    }
    else {
        Write-Host "Keeping folder         : $name (date=$parsedDate)"
    }
}
