# StudentGUI_java

Vision Skills Progression Tracker (Swing desktop app — Java & Maven)

This repository contains a Swing-based desktop application used to collect
and visualize student assessment progress across several skill-based
progressions (Braille, Abacus, Keyboarding, ScreenReader, DigitalLiteracy,
iOS, CVI, etc.). The code lives under `src/main/java/com/studentgui` and
the app stores per-student data under the `app_home/` folder (created on
first run).

This README focuses on helping a non-expert user build the project and
produce a clickable desktop application (native installer or launcher)
for macOS, Windows 11, and common Linux distributions (APT/Pacman/DNF/RPM
families) so they can run the app by double-clicking an icon.

## Quick summary

- Build with Maven (JDK 21 recommended).
- Run the produced shaded (fat) jar with `java -jar`.
- For a truly "clickable" app on each platform, use the JDK `jpackage`
  tool to produce a native installer or application bundle (examples below).

## Prerequisites

- Java Development Kit (JDK) 21 is recommended. If you cannot get 21,
  JDK 17 will often work but some minor features may differ.
- Maven (to build the project).

Install Java with your platform's package manager (examples):

macOS (Homebrew):

```bash
brew update
brew install temurin@21   # or `brew install openjdk` then follow brew notes
```

Windows 11 (winget):

```powershell
winget install --id EclipseAdoptium.Temurin.21JDK -e
# Or download the MSI from Adoptium/Temurin or Microsoft Build of OpenJDK
```

Ubuntu / Debian (apt):

```bash
sudo apt update
# If available on your distro:
sudo apt install openjdk-21-jdk
# Fallback (widely available):
sudo apt install openjdk-17-jdk
```

Arch Linux (pacman):

```bash
sudo pacman -Syu
sudo pacman -S jdk-openjdk
```

Fedora / RHEL (dnf):

```bash
sudo dnf install java-21-openjdk-devel
```

openSUSE (zypper / rpm-based):

```bash
sudo zypper install java-21-openjdk-devel
```

If your distribution doesn't yet ship JDK 21, use the vendor installers
from Adoptium / Eclipse Temurin, or consider SDKMAN (cross-platform).

Verify Java is available:

```bash
java -version
mvn -v
```

## Build the project

From the project root (where `pom.xml` is located):

```bash
# Build and package (skip tests for a faster local build)
mvn -DskipTests package
```

This will produce a packaged jar under `target/`. The project produces a
shaded jar (fat jar) that contains dependencies — look for a file named
like `vision-skills-progression-tracker-<version>.jar` or similar in
`target/`.

## Run the jar directly (simple, cross-platform)

If you just want to run the app without creating a platform installer,
double-clicking the jar is possible once Java is installed and the `.jar`
file type is associated with Java. Otherwise you can run from a terminal:

Windows (PowerShell):

```powershell
java -jar .\target\vision-skills-progression-tracker-1.0.0-beta.jar
```

macOS / Linux (bash/zsh/pwsh):

```bash
java -jar target/vision-skills-progression-tracker-1.0.0-beta.jar
```

Note: replace the jar name above with the actual artifact that was
produced in `target/`.

## Make a clickable native app (recommended) — using jpackage

`jpackage` is included with modern JDKs and creates native installers or
application bundles for macOS, Windows, and many Linux distributions. It
wraps the JRE with your app so end users don't need to install Java.

General tips before running `jpackage`:

- Copy the produced jar into a small `app` folder or use the `target/`
  directory as the input directory.
- If your fat jar contains a proper `Main-Class` in its manifest,
  `jpackage` can automatically start the app. Otherwise pass
  `--main-class` with the fully-qualified main class name.
- Provide an icon file for a nicer-looking app (`.icns` for macOS,
  `.ico` for Windows, and `.png` for Linux).

Example commands (replace placeholders with actual names):

macOS (.dmg or .pkg):

```bash
# Build first
mvn -DskipTests package

# Create a macOS app bundle / dmg (run on macOS machine)
jpackage \
  --type dmg \
  --name "VisionSkillsTracker" \
  --input target \
  --main-jar vision-skills-progression-tracker-1.0.0-beta.jar \
  --icon path/to/icon.icns \
  --app-version 1.0.0
```

Windows (.msi or .exe):

```powershell
# Run on Windows 11 machine with JDK 21 installed
mvn -DskipTests package

jpackage --type msi \
  --name "VisionSkillsTracker" \
  --input target \
  --main-jar vision-skills-progression-tracker-1.0.0-beta.jar \
  --icon path\to\icon.ico \
  --app-version 1.0.0
```

Linux (.deb or .rpm):

```bash
# On Debian/Ubuntu-based systems for a .deb package
jpackage --type deb \
  --name "VisionSkillsTracker" \
  --input target \
  --main-jar vision-skills-progression-tracker-1.0.0-beta.jar \
  --icon path/to/icon.png \
  --app-version 1.0.0

# For RPM-based systems, use --type rpm instead of deb
```

`jpackage` produces a native installer or application bundle which the
end-user can double-click like any other native app. This is the most
painless cross-platform approach for non-technical users.

Important: `jpackage` must be run on the target platform to produce
platform-specific installers (run on mac to make a .dmg/.pkg, on
windows to make .msi/.exe, etc.).

## Simpler alternatives (no installer)

- Windows: create a small `run.bat` next to the jar which runs
  `java -jar "vision-...jar"` and create a shortcut to the `.bat`.
- macOS: wrap the jar into an application with tools like `jar2app`
  (3rd-party) or use `jpackage` as above.
- Linux: create a `.desktop` file that runs `java -jar /path/to/jar`
  (make it executable and place in `~/.local/share/applications/` for
  user-level apps).

Example `.desktop` file (replace paths):

```ini
[Desktop Entry]
Name=VisionSkillsTracker
Comment=Student GUI app
Exec=java -jar /path/to/vision-skills-progression-tracker-1.0.0-beta.jar
Terminal=false
Type=Application
Icon=/path/to/icon.png
Categories=Education;Utility;
```

Make it executable:

```bash
chmod +x ~/.local/share/applications/vision-skills-tracker.desktop
```

## Troubleshooting & tips for non-experts

- Double-clicking a `.jar` only works if Java is installed and the
  file association is set. If double-click doesn't work, run from a
  terminal so you can see error messages.
- macOS Gatekeeper: if macOS prevents the app from opening because it's
  unsigned, right-click the app and choose Open, then confirm.
- Windows SmartScreen: similar warnings may appear for unsigned apps —
  there will usually be an option to run anyway.
- If you get "no main manifest attribute", the jar's manifest doesn't
  specify a Main-Class. Run with `java -cp jarname.jar com.studentgui.Main`
  (replace with the real main class) or rebuild so the shaded jar has
  the main class set.

## Where the app stores files

- Per-student plots: `app_home/StudentDataFiles/{safeName}/plots`
- Reports: `app_home/StudentDataFiles/{safeName}/reports`
- `app_home` is created in the working directory by default; the
  helper constant `com.studentgui.apphelpers.Helpers.APP_HOME` controls
  this behavior.

## Developers / Maintainers notes

- Run unit tests: `mvn test`
- Project entry points and runtime preferences are under
  `src/main/java/com/studentgui` (look for `Main` and the pages under
  `apppages`).

---

## GitHub Actions (CI, Site, Packages)

- `CI`: builds and runs tests on every push/PR.
- `Maven Site`: runs `mvn site` and uploads the generated site (`target/site`) as a workflow artifact. Trigger it via the "Maven Site" workflow (Actions tab → Run workflow). Download the artifact to view locally or host elsewhere.
- `Package Linux`: builds `.deb`, `.rpm`, and attempts an AppImage. Artifacts are uploaded from `dist/linux/`.
- `Package Windows`: builds a `.msi` via `jpackage`. Artifact is uploaded from `dist/windows/`.
- `Package macOS`: builds a `.dmg` via `jpackage`. Artifact is uploaded from `dist/macos/`.
- `Deploy Maven Site`: builds `mvn site` and publishes to GitHub Pages on `main`/`master` or manual run.
- `Release`: on Release publish, builds installers for Linux/Windows/macOS and attaches them to the GitHub Release automatically.

Notes:
- Linux DMG is not a standard format; DMG is macOS-only. On Linux, prefer `.deb`, `.rpm`, or AppImage.
- These packaging workflows follow the templates under `examples/` and run `jpackage` on the appropriate OS runner to produce native installers.
- The displayed app name is taken from `pom.xml <name>`; if absent, it falls back to `VisionSkillsTracker`. Icons are sourced from `examples/icons`.

 