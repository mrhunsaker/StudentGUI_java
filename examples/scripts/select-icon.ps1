<#!
.SYNOPSIS
  Selects the best icon size from build/icons for a target packaging type.
.DESCRIPTION
  Given a desired size (default 256) and fallback order, picks the closest available PNG or ICO.
  Writes a properties file (selected-icon.properties) with icon.win, icon.mac, icon.linux entries.
.PARAMETER DesiredSize
  Preferred size (e.g. 256, 512).
.PARAMETER Output
  Properties file to write (default selected-icon.properties).
.EXAMPLE
  pwsh scripts/select-icon.ps1 -DesiredSize 512
#>
param(
  [int]$DesiredSize = 256,
  [string]$Output = "selected-icon.properties"
)
$iconDir = Join-Path (Get-Location) 'graph-digitizer-java/build/icons'
if (!(Test-Path -LiteralPath $iconDir)) { Write-Error "Icon directory not found: $iconDir"; exit 1 }
$files = Get-ChildItem -LiteralPath $iconDir -File | Where-Object { $_.Name -match 'scatter-plot-(\d+)\.(png|ico)$' }
if (-not $files) { Write-Error "No scatter-plot icons found"; exit 1 }
$parsed = $files | ForEach-Object {
  if ($_.Name -match 'scatter-plot-(\d+)\.(png|ico)$') {
    [pscustomobject]@{File=$_.FullName; Size=[int]$matches[1]; Ext=$_.Extension.ToLower()}
  }
} | Sort-Object Size
# pick best match >= desired else largest below
$best = $parsed | Where-Object { $_.Size -ge $DesiredSize } | Select-Object -First 1
if (-not $best) { $best = $parsed | Select-Object -Last 1 }
$linux = ($parsed | Where-Object { $_.Ext -eq '.png' -and $_.Size -ge $DesiredSize } | Select-Object -First 1)
if (-not $linux) { $linux = $parsed | Where-Object { $_.Ext -eq '.png' } | Select-Object -Last 1 }
$mac = $linux # placeholder until .icns generated externally
$win = ($parsed | Where-Object { $_.Ext -eq '.ico' -and $_.Size -ge $DesiredSize } | Select-Object -First 1)
if (-not $win) { $win = $parsed | Where-Object { $_.Ext -eq '.ico' } | Select-Object -Last 1 }
"icon.win=$($win.File)" | Out-File -FilePath $Output -Encoding UTF8
"icon.mac=$($mac.File)" | Out-File -FilePath $Output -Encoding UTF8 -Append
"icon.linux=$($linux.File)" | Out-File -FilePath $Output -Encoding UTF8 -Append
Write-Host "Selected icons written to $Output"