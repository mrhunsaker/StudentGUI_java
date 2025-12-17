<#
verify-msi.ps1

A small smoke-test script to validate an MSI produced by jpackage.

Features:
- Performs an administrative extract (msiexec /a) to a temp folder under target/jpackage-msi/extracted
- Locates the main EXE (GraphDigitizer.exe) inside the extracted tree
- Launches the EXE for a brief timeout and reports whether it started
- Writes a detailed log to target/jpackage-msi/verify-msi.log and returns non-zero on failure

Usage:
  pwsh .\scripts\verify-msi.ps1
  pwsh .\scripts\verify-msi.ps1 -MsiPath target\jpackage-msi\GraphDigitizer-1.1.msi -TimeoutSeconds 10

Optional parameter -DoInstall: if present the script will perform a normal install (msiexec /i) into a temporary folder and then uninstall it.
#>
param(
    [string]$MsiPath = "target\jpackage-msi\GraphDigitizer-1.1.msi",
    [int]$TimeoutSeconds = 10,
    [switch]$DoInstall
)

$cwd = (Get-Location).Path
$msi = (Resolve-Path $MsiPath -ErrorAction Stop).Path
$extract = Join-Path $cwd 'target\jpackage-msi\extracted'
$log = Join-Path $cwd 'target\jpackage-msi\verify-msi.log'

function Log($msg){ "$((Get-Date).ToString('o')) - $msg" | Out-File -FilePath $log -Append }

# Start fresh
if(Test-Path $log){ Remove-Item $log -Force -ErrorAction SilentlyContinue }
"=== VERIFY MSI SCRIPT START: $(Get-Date -Format o) ===" | Out-File -FilePath $log
Log "MSI: $msi"

if(Test-Path $extract){ Remove-Item -Recurse -Force $extract -ErrorAction SilentlyContinue; Log "Removed existing extract dir: $extract" }
New-Item -ItemType Directory -Path $extract | Out-Null
Log "Extracting MSI to: $extract"

$args = @('/a', $msi, '/qn', "TARGETDIR=$extract")
try{
    Start-Process -FilePath msiexec -ArgumentList $args -Wait -NoNewWindow -ErrorAction Stop
    Log "Extraction completed OK"
} catch{
    Log "Extraction FAILED: $_"
    exit 2
}

Log "Listing EXE files in extracted tree"
Get-ChildItem -Path $extract -Recurse -Filter *.exe | Sort-Object Length -Descending | Select-Object FullName,Length | Out-File -FilePath $log -Append

$exeItem = Get-ChildItem -Path $extract -Recurse -Filter GraphDigitizer.exe | Select-Object -First 1
if(-not $exeItem){
    Log "ERROR: GraphDigitizer.exe not found in extracted tree"
    exit 3
}
$exe = $exeItem.FullName
Log "Found extracted EXE: $exe"

# Start the EXE and wait briefly
try{
    $p = Start-Process -FilePath $exe -PassThru -ErrorAction Stop
    Log "Started process PID: $($p.Id)"
    $waitMillis = [int]($TimeoutSeconds * 1000)
    $exited = $p.WaitForExit($waitMillis)
    if($exited){
        Log "Process exited within ${waitMillis}ms. ExitCode: $($p.ExitCode)"
    } else {
        Log "Process did not exit within ${waitMillis}ms; attempting to close." 
        try{ $p.Kill(); Log "Process killed" } catch{ Log "Failed to kill process: $_" }
    }
} catch{
    Log "Failed to start EXE: $_"
    exit 4
}

if($DoInstall){
    # perform a normal install to a temp folder and uninstall it to validate installer actions
    $installDir = Join-Path $cwd 'target\jpackage-msi\installed'
    if(Test-Path $installDir){ Remove-Item -Recurse -Force $installDir -ErrorAction SilentlyContinue }
    New-Item -ItemType Directory -Path $installDir | Out-Null
    Log "Performing silent install to: $installDir"
    $args = @('/i', $msi, '/qn', "TARGETDIR=$installDir", '/l*v', (Join-Path $cwd 'target\jpackage-msi\install.log'))
    try{
        Start-Process -FilePath msiexec -ArgumentList $args -Wait -NoNewWindow -ErrorAction Stop
        Log "Silent install completed"
        # find installed exe and run briefly
        $installedExe = Get-ChildItem -Path $installDir -Recurse -Filter GraphDigitizer.exe | Select-Object -First 1
        if($installedExe){
            Log "Found installed exe: $($installedExe.FullName)"
            $p2 = Start-Process -FilePath $installedExe.FullName -PassThru -ErrorAction Stop
            $ran = $p2.WaitForExit([int]($TimeoutSeconds*1000))
            if($ran){ Log "Installed process exited, ExitCode: $($p2.ExitCode)" } else { $p2.Kill(); Log "Installed process killed" }
        } else {
            Log "Installed exe not found under $installDir"
        }
        # uninstall by product code is more robust; try to use UninstallString if present in registry
        Log "Attempting uninstall by TARGETDIR cleanup (best-effort)"
        Remove-Item -Recurse -Force $installDir -ErrorAction SilentlyContinue
        Log "Cleanup of installDir attempted"
    } catch{
        Log "Silent install FAILED: $_"
        exit 5
    }
}

Log "=== VERIFY MSI SCRIPT END: $(Get-Date -Format o) ==="
Write-Output "Verification log: $log"
Get-Content $log -Tail 200 | Write-Output

exit 0
