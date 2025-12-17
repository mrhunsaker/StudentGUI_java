<#!
.SYNOPSIS
    Archives old Graph Digitizer log files (text & gz) into a timestamped zip.
.DESCRIPTION
    Finds log files in .\logs matching graph-digitizer*.log or .log.gz older than -Days threshold.
    Moves them into an archive zip (logs/archive-YYYYMMDD-HHMMSS.zip). Skips active current log.
.PARAMETER Days
    Minimum age in days for a log file to be archived. Default: 7
.PARAMETER DryRun
    If specified, shows which files would be archived without performing changes.
.EXAMPLE
    ./archive-logs.ps1 -Days 14
.EXAMPLE
    ./archive-logs.ps1 -DryRun
#>
param(
    [int]$Days = 7,
    [switch]$DryRun
)
$logDir = Join-Path -Path (Get-Location) -ChildPath 'logs'
if (!(Test-Path -LiteralPath $logDir)) {
    Write-Error "Log directory not found: $logDir"; exit 1
}
$cutoff = (Get-Date).AddDays(-$Days)
$files = Get-ChildItem -LiteralPath $logDir -File | Where-Object {
    ($_).Name -match '^graph-digitizer.*\.log(\.gz)?$' -and ($_.LastWriteTime -lt $cutoff)
}
if (-not $files) { Write-Host "No log files older than $Days days."; exit 0 }
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$archiveName = "archive-$timestamp.zip"
$archivePath = Join-Path $logDir $archiveName
Write-Host "Preparing archive: $archivePath"
foreach ($f in $files) {
    if ($DryRun) { Write-Host "[DRY] Include: $($f.Name)"; continue }
}
if ($DryRun) { Write-Host "Dry run complete."; exit 0 }
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($archivePath, 'Create')
foreach ($f in $files) {
    Write-Host "Archiving: $($f.Name)"
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $f.FullName, $f.Name)
    Remove-Item -LiteralPath $f.FullName -Force
}
$zip.Dispose()
Write-Host "Archive created: $archivePath"
