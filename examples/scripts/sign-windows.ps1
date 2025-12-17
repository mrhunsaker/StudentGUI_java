<#!
.SYNOPSIS
  Code signs Windows executables produced by jpackage.
.DESCRIPTION
  Wraps signtool.exe invocation. Requires a code signing certificate (.pfx) and password.
.PARAMETER CertPath
  Path to PFX certificate file.
.PARAMETER Password
  Password for the PFX (use ENV var or secret manager).
.PARAMETER TimestampUrl
  RFC3161 timestamp server URL (default: https://timestamp.digicert.com).
.EXAMPLE
  pwsh scripts/sign-windows.ps1 -CertPath certs/code-signing.pfx -Password $env:PFX_PW
#>
param(
  [Parameter(Mandatory=$true)][string]$CertPath,
  [Parameter(Mandatory=$true)][securestring]$Password,
  [string]$TimestampUrl = 'https://timestamp.digicert.com'
)
$exeFiles = Get-ChildItem -Path 'graph-digitizer-java/target' -Filter '*.exe' -ErrorAction SilentlyContinue
if (-not $exeFiles) { Write-Warning 'No .exe files found to sign.'; exit 0 }
foreach ($f in $exeFiles) {
  Write-Host "Signing $($f.Name)"
  $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password)
  $pwPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
  try {
    & signtool sign /fd SHA256 /f "$CertPath" /p "$pwPlain" /tr "$TimestampUrl" /td SHA256 "$($f.FullName)" || throw "Signing failed for $($f.Name)"
  } finally {
    [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
  }
}
Write-Host 'Signing complete.'
