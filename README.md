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
		help reveal overlapping points. The rendering-only constant is
		`JLineGraph.JITTER_AMPLITUDE` (set to `0.10d`). This affects only
		the displayed points, not the stored database values.
	- Background bands: horizontal colored bands have been changed to the
		ranges requested by the UI spec: red = -0.25..0.5, orange = 0.5..1.5,
		orange = 1.5..2.5, yellow = 2.5..3.5, green = 3.5..4.5.
	- Y-axis limits: charts now use the Y-range `-0.25` .. `4.25` to show
		the bands and provide visual breathing room.

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
java -jar .\target\vision-skills-progression-tracker-1.0.0-beta.jar
```

Where the app stores files and plots
- Per-student plots are saved under `app_home/StudentDataFiles/{safeName}/plots`.
- Session markdown and HTML reports are written to `app_home/StudentDataFiles/{safeName}/reports`.
- `app_home` is created in the repository working directory by default; see `com.studentgui.apphelpers.Helpers.APP_HOME`.

Configuration & toggles
- To change the default roster used at startup, edit `json_Files/students.json`.
- The jitter is a rendering-only constant (see `JLineGraph.JITTER_AMPLITUDE`).
	If you want to temporarily disable jitter for exported charts, you can
	set that constant to `0.0` and rebuild. A future enhancement is to add a
	runtime toggle in the UI to enable/disable jitter without rebuilding.

Notes for maintainers
- The `Helpers.defaultStudent()` method is used to provide a non-null
	default student name when a page is constructed without a student.
- The Braille page previously used fixed-size arrays when creating the
	assessment insert arrays which could be out-of-sync with the parts
	list; that has been fixed so plotted columns match database columns.
- `JLineGraph` applies jitter only to displayed values (it never mutates
	persisted session data).

If you'd like me to add a user-facing toggle (checkbox) to control the
jitter at runtime, or to make jitter deterministically seeded for
reproducible exported plots, say the word and I will add it.