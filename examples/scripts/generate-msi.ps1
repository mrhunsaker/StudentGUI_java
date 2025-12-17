Param(
    [string]$AppName = "graph-digitizer",
    [string]$AppVersion = "1.0.0",
    [string]$RuntimeImageX64 = "",
    [string]$RuntimeImageArm64 = ""
)

if (-not $env:JAVA_HOME -or [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    Write-Error "JAVA_HOME is not set. Please set JAVA_HOME to your JDK installation before running this script. Example: $env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'"
    exit 1
}

$jpackage = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
if (-not (Test-Path $jpackage)) {
    Write-Error "jpackage not found at $jpackage. Please ensure your JDK includes jpackage and JAVA_HOME points to it."
    exit 1
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$targetDir = Join-Path $scriptDir "..\target"

$jar = Get-ChildItem -Path $targetDir -Filter "*.jar" -File | Where-Object { $_.Name -notmatch 'sources|original|tests' } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $jar) {
    Write-Error "No application JAR found under $targetDir. Build the project first (e.g. mvn package)."
    exit 1
}

$outDir = Join-Path $scriptDir "..\target\generated_builds"
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir -Force | Out-Null }

# Determine runtime images: prefer explicit parameters, fallback to environment variables
if ([string]::IsNullOrWhiteSpace($RuntimeImageX64)) { $RuntimeImageX64 = $env:RUNTIME_IMAGE_X64 }
if ([string]::IsNullOrWhiteSpace($RuntimeImageArm64)) { $RuntimeImageArm64 = $env:RUNTIME_IMAGE_ARM64 }

$arches = @(
    @{ Name = 'x64'; Runtime = $RuntimeImageX64; Suffix = 'x64' },
    @{ Name = 'arm64'; Runtime = $RuntimeImageArm64; Suffix = 'arm64' }
)

$overallExit = 0
foreach ($arch in $arches) {
    $name = $arch.Name
    $runtime = $arch.Runtime
    $suffix = $arch.Suffix

    if ([string]::IsNullOrWhiteSpace($runtime)) {
        Write-Warning "Skipping $name: no runtime image provided. Set -RuntimeImage$($suffix.ToUpper()) or environment variable RUNTIME_IMAGE_$($suffix.ToUpper())."
        $overallExit = 2
        continue
    }

    if (-not (Test-Path $runtime)) {
        Write-Warning "Runtime image path for $name does not exist: $runtime. Skipping."
        $overallExit = 2
        continue
    }

    Write-Host "Generating MSI for architecture: $name using runtime image: $runtime"

    # Run jpackage with the provided runtime image
    & $jpackage --type msi --input $jar.DirectoryName --main-jar $jar.Name --name $AppName --app-version $AppVersion --dest $outDir --runtime-image $runtime @Args

    if ($LASTEXITCODE -ne 0) {
        Write-Error "jpackage failed for $name with exit code $LASTEXITCODE"
        $overallExit = $LASTEXITCODE
        continue
    }

    # Find the newest MSI produced in the output directory and rename it to include arch suffix
    $msi = Get-ChildItem -Path $outDir -Filter "*.msi" -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($msi) {
        $newName = "{0}-{1}-{2}{3}" -f $AppName, $AppVersion, $suffix, $msi.Extension
        $newPath = Join-Path $outDir $newName
        try {
            Move-Item -Path $msi.FullName -Destination $newPath -Force
            Write-Host "MSI created: $newPath"
        } catch {
            Write-Warning "Failed to rename MSI $($msi.FullName) to $newPath: $_"
            Write-Host "MSI left at: $($msi.FullName)"
        }
    } else {
        Write-Warning "No MSI found in $outDir after jpackage for $name"
    }
}

exit $overallExit
