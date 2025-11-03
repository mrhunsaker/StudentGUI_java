# StudentGUI_java

Vision Skills Progression Tracker (StudentGUI_java)

This project is a Swing-based desktop application (Java 21, Maven) that
helps collect and visualize student assessment progress across a set of
skill-based progressions (Braille, Abacus, Keyboarding, ScreenReader,
DigitalLiteracy, iOS, CVI, and others).

This repository contains the UI pages under `src/main/java/com/studentgui/apppages`, helper classes under
`src/main/java/com/studentgui/apphelpers`, and a small embedded SQLite-backed
database used to persist assessment sessions.

Summary of recent changes (refactor/exception-cleanup branch)

- Default student selection: UI pages now fall back to the first roster
	entry when opened with a `null` or empty student name. The helper
	`com.studentgui.apphelpers.Helpers.defaultStudent()` returns the first
	entry from `json_Files/students.json` (or a sensible fallback).

- Braille submit fix: `Braille.submitData()` now allocates the codes and
	scores arrays dynamically using the actual `partCodes.length` so the
	data written to the normalized schema matches what the plotting logic
	expects. This removes earlier mismatches caused by fixed-size arrays.

````markdown
# StudentGUI_java

Vision Skills Progression Tracker (StudentGUI_java)

This project is a Swing-based desktop application (Java 21, Maven) that
helps collect and visualize student assessment progress across a set of
skill-based progressions (Braille, Abacus, Keyboarding, ScreenReader,
DigitalLiteracy, iOS, CVI, and others).

This repository contains the UI pages under `src/main/java/com/studentgui/apppages`, helper classes under
`src/main/java/com/studentgui/apphelpers`, and a small embedded SQLite-backed
database used to persist assessment sessions.

Summary of recent changes (refactor/exception-cleanup branch)

- Default student selection: UI pages now fall back to the first roster
	entry when opened with a `null` or empty student name. The helper
	`com.studentgui.apphelpers.Helpers.defaultStudent()` returns the first
	entry from `json_Files/students.json` (or a sensible fallback).

- Braille submit fix: `Braille.submitData()` now allocates the codes and
	scores arrays dynamically using the actual `partCodes.length` so the
	data written to the normalized schema matches what the plotting logic
	expects. This removes earlier mismatches caused by fixed-size arrays.

- Plotting improvements in `JLineGraph`:

	- Jitter: plotted points receive a small visual jitter of ±0.10 to
		help reveal overlapping points. This is a rendering-only effect
		and does not change stored database values. Jitter can be toggled
		at runtime via the centralized Preferences dialog and can be made
		deterministic/seeded for reproducible exported charts.

	- Background bands: horizontal colored bands have been added and the
		Y-axis limits are set to `-0.25 .. 4.25` to show the bands and
		provide visual breathing room.

- Runtime preferences and centralization:

	- `PreferencesDialog` is the canonical runtime UI for plot and UI
		settings. It persists `jitter.enabled`, `jitter.deterministic` and
		`jitter.seed` to the app properties and calls
		`Main.notifySettingsChanged()` so live components apply new settings.

	- `SettingsChangeListener` is used to notify live components; the
		shared `JLineGraph` is registered at startup by `Main`.

- UI theming:

	- Added a guarded "Material Theme UI Lite" submenu to the Themes menu
		that exposes a curated list of Material themes when the
		corresponding classes are present on the classpath.

	- Theme selection persists to app properties and is applied at
		startup.

How to build

Open a PowerShell terminal and run the following (from the project root):

```powershell
# compile and package (skip tests for a quicker cycle)
mvn -DskipTests package

# the shaded jar will be produced in target/ (name includes the project id)
```

How to run the packaged jar

After building, run the shaded jar with Java 21 (example PowerShell):

```powershell
# replace the jar name with the artifact produced in target/
java -jar .\\target\\vision-skills-progression-tracker-1.0.0-beta.jar
```

Where the app stores files and plots

- Per-student plots are saved under `app_home/StudentDataFiles/{safeName}/plots`.
- Session markdown and HTML reports are written to `app_home/StudentDataFiles/{safeName}/reports`.
- `app_home` is created in the repository working directory by default; see `com.studentgui.apphelpers.Helpers.APP_HOME`.

Configuration & toggles

- To change the default roster used at startup, edit `json_Files/students.json`.

- Runtime jitter controls: use the centralized `PreferencesDialog` (menu -> Themes/Preferences depending on platform) to enable/disable jitter at runtime, switch deterministic mode, and set a seed. The preferences are persisted under:

	- `jitter.enabled` = true/false
	- `jitter.deterministic` = true/false
	- `jitter.seed` = long integer (optional)

	These settings are applied immediately to the registered `JLineGraph` instance(s).

Notes for maintainers

- The `Helpers.defaultStudent()` method is used to provide a non-null
	default student name when a page is constructed without a student.
- The Braille page previously used fixed-size arrays when creating the
	assessment insert arrays which could be out-of-sync with the parts
	list; that has been fixed so plotted columns match database columns.
- `JLineGraph` applies jitter only to displayed values (it never mutates
	persisted session data).

Testing & smoke utility

- Unit tests: run `mvn test` — the branch's local run shows all unit
	tests passing (11 tests, 0 failures). A smoke utility (`SmokeTest`) can
	be executed to validate chart PNG export and confirms files are written
	to `app_home/StudentDataFiles/Smoke Test/plots`.

If you'd like I can add CI to run `mvn test` on PRs and make javadoc
warnings fail the build; say which option you prefer and I'll implement it.
````