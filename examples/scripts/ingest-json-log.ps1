<#!
.SYNOPSIS
    Simple PowerShell ingestion example for the newline-delimited JSON log produced by Graph Digitizer.
.DESCRIPTION
    Reads the file logs/graph-digitizer.json (or a supplied -Path) and filters by Level or Logger.
    Demonstrates how structured logs can be piped into other tools.
.PARAMETER Path
    Path to the JSON log file (default: logs/graph-digitizer.json)
.PARAMETER Level
    Optional level filter (e.g. INFO, DEBUG, ERROR)
.PARAMETER Logger
    Optional logger name prefix filter (e.g. com.digitizer)
.EXAMPLE
    ./ingest-json-log.ps1 -Level ERROR
.EXAMPLE
    ./ingest-json-log.ps1 -Logger com.digitizer.core
#>
param(
    [string]$Path = "logs/graph-digitizer.json",
    [string]$Level,
    [string]$Logger
)

if (!(Test-Path -LiteralPath $Path)) {
    Write-Error "Log file not found: $Path"; exit 1
}

Get-Content -LiteralPath $Path | ForEach-Object {
    if ([string]::IsNullOrWhiteSpace($_)) { return }
    try {
        $obj = $_ | ConvertFrom-Json -ErrorAction Stop
    } catch {
        Write-Warning "Skipping malformed line"; return
    }
    if ($Level -and $obj.level -ne $Level) { return }
    if ($Logger -and ($obj.logger -notlike "$Logger*")) { return }
    [pscustomobject]@{
        Time   = $obj.time
        Level  = $obj.level
        Logger = $obj.logger
        Thread = $obj.thread
        Msg    = $obj.message
    }
} | Format-Table -AutoSize
