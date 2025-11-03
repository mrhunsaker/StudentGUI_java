# Changelog

All notable changes to this project should be documented in this file.
This changelog uses a simple, developer-friendly format and is intended
for maintainers reviewing branches and releases.

## [Unreleased] - refactor/exception-cleanup

### Added

- PreferencesDialog: a centralized runtime preferences UI that persists
  settings to the app properties and notifies running components when
  preferences change. The dialog exposes jitter controls (enable/disable,
  deterministic/seeded) and persists the choices under the `jitter.*`
  keys.
- SettingsChangeListener: a small listener interface used to notify
  components about runtime preference changes. `JLineGraph` implements
  this interface and applies persisted settings when notified.
- Deterministic jitter unit test: `JLineGraphDeterministicJitterTest`
  verifies that two `JLineGraph` instances seeded identically produce the
  same jitter sequence (render-only behavior is validated via reflection).

### Changed

- JLineGraph: jitter rendering (±0.10) added as a visual-only effect; new
  public APIs to toggle jitter and set the seed (`setJitterEnabled`,
  `setJitterDeterministic`, `setJitterSeed`). Horizontal background bands
  were added and Y-axis limits are now set to `-0.25 .. 4.25` to match the
  visual band ranges.
- Preferences moved to a single canonical UI: the per-chart control bar was
  removed from `JLineGraph` in favor of the centralized
  `PreferencesDialog`, which persists settings and triggers
  `Main.notifySettingsChanged()` so live components update immediately.
- Theme menu: added a guarded "Material Theme UI Lite" submenu exposing a
  curated list of Material theme class names when the corresponding
  classes are present on the classpath.
- README.md: updated build/run instructions and documented the new
  runtime preferences and plotting behavior.

### Fixed

- Braille: fixed submitData sizing bug — arrays for part codes and scores
  are now allocated dynamically using the actual parts length so persisted
  assessment rows align with plotted series.

### Quality / Docs

- Javadoc cleanup: multiple small JavaDoc additions and cleanups were
  applied across utility classes and app pages to remove doclint warnings.

### Tests

- Unit tests: local test run shows all unit tests passing (11 tests,
  0 failures). A smoke utility was added/used to validate chart PNG
  export under `app_home/StudentDataFiles/Smoke Test/plots`.

### Notes

- Jitter is strictly a rendering effect — stored session data is never
  mutated. The runtime preferences are stored under `jitter.enabled`,
  `jitter.deterministic`, and `jitter.seed` in the app properties file.
- `Main` exposes `addSettingsChangeListener(...)` /
  `removeSettingsChangeListener(...)` and registers the shared
  `JLineGraph` instance at startup; if pages later create page-local
  `JLineGraph` instances they should register/unregister them with
  `Main` to receive runtime preference updates.
