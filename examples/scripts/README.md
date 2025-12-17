# Packaging helper scripts

This folder contains small helper scripts that wrap `jpackage` and related tooling to produce native installers and app-images for different platforms.

## Overview


- All script outputs are written to `target/generated_builds` (previously `scripts/out`).

- Scripts prefer to dereference symlinks when copying jpackage app-image contents: they use `rsync -aL` when available, and fall back to `cp -rL`.

## Scripts of interest


- `generate-appimage.sh` — produce an `app-image` (and optionally an AppImage if `appimagetool` is installed).

- `generate-deb.sh` — produce a `.deb` package via `jpackage --type deb`.

- `generate-rpm.sh` — produce a `.rpm` package via `jpackage --type rpm` or fallback to `app-image` + `fpm`.

- `generate-dmg.sh` — macOS `.dmg` using `jpackage --type dmg` (run on macOS).

- `generate-snap.sh` — create a snap from the `app-image` via `snapcraft` (if available).

- `generate-msi.ps1` — PowerShell script to create Windows MSI installers.

## Important: Windows MSI multi-arch behavior


- `generate-msi.ps1` can produce up to two MSIs in one run: one for `x64` and one for `arm64`.

- The script requires per-architecture runtime images (produced by `jlink` or downloaded) and accepts them via parameters:

  - `-RuntimeImageX64 <path>`

  - `-RuntimeImageArm64 <path>`

- If a runtime image is not supplied or the path doesn't exist the script will skip that architecture and emit a warning (this is the intended behavior).

- Example (both arches):

```powershell
.\scripts\generate-msi.ps1 -AppVersion 1.2.3 -RuntimeImageX64 C:\runtimes\win-x64\jre -RuntimeImageArm64 C:\runtimes\win-arm64\jre

```


- Example (only x64):

```powershell
.\scripts\generate-msi.ps1 -AppVersion 1.2.3 -RuntimeImageX64 C:\runtimes\win-x64\jre

```

## Where outputs appear


- `target/generated_builds` will contain generated files and folders. MSI files will be renamed to include the architecture, for example:

  - `graph-digitizer-1.2.3-x64.msi`

  - `graph-digitizer-1.2.3-arm64.msi`

## Creating runtime images


- Packaging multi-arch MSIs requires runtime images matching each architecture. See `docs/JPACKAGE.md` for `jlink` and Docker examples for producing `arm64` runtime images on x86_64 hosts.

## Troubleshooting


- If `jpackage` fails with permission or symlink errors, ensure `rsync` is installed (on Unix) or that `cp -rL` is available. The scripts try to avoid creating symlinks in the repo tree.

- For Windows MSI creation, ensure WiX toolset is installed on the build machine if your Maven flow uses WiX (this script uses `jpackage` directly and expects runtime images).

If you'd like, open an issue or request in the repository and I can add CI examples to produce runtime images and MSIs per-release.
