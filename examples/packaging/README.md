# Packaging Resources

This directory contains supplemental packaging assets and templates used to produce native installers and portable distributions beyond the default jpackage outputs.

See `docs/JPACKAGE.md` for detailed `jlink` / `jpackage` guidance and cross-architecture runtime image examples.

## Files

 | File | Purpose |
 | ------ | --------- |
 | `graph-digitizer.desktop` | Freedesktop desktop entry used for Linux menu integration and AppImage. |
 | `appimage-builder.yml` | Template consumed by `appimage-builder` to generate an AppImage from jpackage app-image output. |
 | `deb/postinst`, `deb/prerm` | Maintainer scripts (templates) for Debian packages (install desktop entry & icon caches, cleanup). |
 | `rpm/graph-digitizer.spec` | RPM spec template describing metadata, files, scriptlets. |
 | `../build/icons/` | Icon assets (copied automatically from repository root `icons/` by Maven during `prepare-package`). |
 | `selected-icon.properties` | Optional generated properties overriding `icon.win`, `icon.mac`, `icon.linux`. Fallback file provided. |

## Desktop Entry (`graph-digitizer.desktop`)

The desktop file declares:


- `Name`, `Comment`: Display metadata

- `Exec`: Launcher name produced by jpackage (`graph-digitizer`)

- `Icon`: Logical icon reference; when installed system-wide place icon under `usr/share/icons/hicolor/...`

- `Categories`: Classification for desktop environment menus

For DEB/RPM packaging you may optionally install this file as part of a post-install step. For AppImage it is bundled automatically by the build script.

## AppImage Template (`appimage-builder.yml`)

The template assumes you have run:

```bash
mvn -Pnative -Djpackage.type=app-image package

```

This produces `graph-digitizer-java/target/GraphDigitizer` which becomes the ingredient for AppImage creation. The template script section:


1. Copies desktop entry into `usr/share/applications`

2. Installs a 256x256 icon into `usr/share/icons/hicolor/256x256/apps/`

3. Leaves remaining runtime contents under `usr/bin`

### Build Example

```bash
# Install appimage-builder (refer to official docs)
appimage-builder --recipe graph-digitizer-java/packaging/appimage-builder.yml

```

Resulting artifact: `GraphDigitizer-x86_64.AppImage` (optionally create a `.zsync` file for delta updates).

### Adding Delta Updates (.zsync)

AppImage can support binary delta updates via a companion `.zsync` file. After building the AppImage:

```bash
# Extract update information (optional embedded metadata)
export APPIMAGE=GraphDigitizer-x86_64.AppImage

# Generate .zsync using appimagetool (needs AppImageKit/appimagetool installed)
appimagetool --generate-update-info "$APPIMAGE" > update-info.txt || true
appimagetool --embed-update-information update-info.txt "$APPIMAGE"
appimagetool --create-zsync "$APPIMAGE"

```

Host both the `.AppImage` and `.AppImage.zsync` at a stable URL. Users of AppImage update tools (e.g. `AppImageUpdate`) can then perform efficient incremental updates.

## Icon Management

Icons are maintained in repository root `icons/` (multiple sizes .png/.ico). During build:


- `maven-resources-plugin` copies them to `graph-digitizer-java/build/icons/`

- Optional selection script `scripts/select-icon.ps1` chooses best sizes and writes `selected-icon.properties`

- `properties-maven-plugin` reads that properties file (if present) at `initialize` phase overriding `icon.win`, `icon.mac`, `icon.linux`

- macOS specific `.icns` is generated in CI or locally with `scripts/create-mac-iconset.sh`

- Fallback `selected-icon.properties` is auto-created if absent so builds never fail; generate a tailored one via `scripts/select-icon.ps1 -DesiredSize 512` on Windows.

## CI Workflow Integration

See `.github/workflows/ci.yml`:


- Linux job builds JAR, DEB, RPM, AppImage (.AppImage + optional .zsync generation step can be added).

- macOS job generates `.icns` prior to DMG packaging.

- Windows job runs icon selection script then builds EXE.

Artifacts are uploaded separately (`build-linux`, `build-macos`, `build-windows`). See the workflow file for the AppImage build using `appimage-builder`.

### Adding Automatic Signing (Optional)

Integrate signing by inserting steps that invoke:

```pwsh
pwsh scripts/sign-windows.ps1 -PfxPath certs/code_signing.pfx -PasswordEnvVar WINDOWS_CERT_PASS -Files (Get-ChildItem target -Filter *.exe).FullName

```

```bash
./scripts/sign-macos.sh GraphDigitizer.app "Developer ID Application: Your Company" "teamid123" GraphDigitizer.dmg

```

Provide secrets via CI (e.g. GitHub Actions encrypted secrets) and guard steps with `if: startsWith(github.ref, 'refs/tags/')` for releases.

### Notarization & Stapling (macOS)

`sign-macos.sh` handles codesign, notarization submission, polling, and stapling. Ensure `xcrun altool` / `notarytool` authentication is configured via keychain or environment variables.

## Extending Packaging


- Add post-install scripts for DEB/RPM: supply maintainer scripts to install `graph-digitizer.desktop`, run `update-desktop-database` and refresh icon caches (see provided `deb/postinst`, `deb/prerm`).

- Add AppImage update metadata: edit `update-information` field in template.

- Add additional icon sizes: place new PNGs/ICOs into root `icons/` naming pattern `scatter-plot-SIZE.png`.

- Add Windows / macOS code signing: use provided scripts to sign executables / DMG.

- Add `.zsync` generation and publish both `.AppImage` and `.AppImage.zsync`.

## Verification Checklist


- [ ] Icons copied to `build/icons`

- [ ] `selected-icon.properties` loaded (if generated)

- [ ] `.icns` generated on macOS (or provided manually)

- [ ] AppImage built and launches `graph-digitizer` binary

- [ ] Desktop file present in AppImage (check with `--appimage-extract`)

- [ ] `.zsync` file generated for AppImage (optional)

- [ ] DEB postinst installs desktop entry & icons

- [ ] RPM spec includes desktop file & icon paths

- [ ] Windows EXE signed (optional)

- [ ] macOS DMG / app signed & notarized (optional)

## Troubleshooting

 | Issue | Resolution |
 | ------- | ------------ |
 | AppImage fails to start | Ensure executable `graph-digitizer` exists in jpackage app-image output. |
 | Icon missing in DEB/RPM menu | Verify icon installed under `usr/share/icons/hicolor/256x256/apps/graph-digitizer.png` and run `update-icon-caches`. |
 | DMG shows generic icon | Confirm `.icns` path passed via `-Dicon.mac` and file contains expected sizes. |
 | Properties file not loaded | Ensure `selected-icon.properties` is at module root `graph-digitizer-java/` when Maven runs. |
 | AppImage update fails | Verify `.AppImage.zsync` hosted at correct URL referenced by embedded update info. |
 | Unsigned binary warnings | Integrate signing scripts in CI and validate certificate chain. |
 | RPM install missing icon | Add `%{_datadir}/icons/hicolor/256x256/apps/graph-digitizer.png` to `%files` in spec. |

## License

All packaging assets are distributed under the project Apache 2.0 license unless otherwise noted.
