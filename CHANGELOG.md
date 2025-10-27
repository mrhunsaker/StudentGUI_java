# Changelog

All notable changes to this project should be documented in this file.
This changelog uses a simple, developer-friendly format and is intended
for maintainers reviewing branches and releases.

## [Unreleased] - refactor/exception-cleanup

### Added
- JLineGraph: runtime plot options control bar with a "Jitter" checkbox,
  "Deterministic" checkbox, and optional seed field. This allows users to
  enable/disable rendering jitter at runtime and to request seeded jitter
  for reproducible exported charts.
- CHANGELOG.md: developer-facing change log (this file).

### Changed
- README.md: expanded build/run instructions and documented recent UI and
  plotting changes (default student, Braille fix, jitter, bands, Y-limits).
- REPORT-pages-db-methods.md: appended "Recent changes" section describing
  the Braille fix, default student behavior, jitter, and band ranges.
- JLineGraph.java: added jitter configuration API (`setJitterEnabled`,
  `setJitterDeterministic`, `setJitterSeed`), deterministic RNG support,
  and improved JavaDoc describing the public update methods and visual
  banding semantics.
- Helpers.defaultStudent(): documented the helper and added Javadoc for
  path helper methods.
- Braille.submitData(): documented the dynamic allocation fix in JavaDoc.

### Fixed
- Braille: replaced earlier fixed-size arrays with dynamic arrays sized to
  `partCodes.length` when persisting assessment results to prevent
  mismatches between persisted columns and plotted series.

### Notes
- Javadoc generation during the build produces warnings for some public
  methods that lack JavaDoc tags; these have been incrementally addressed
  for common helpers and core components. Additional cleanup of javadocs
  across the codebase is recommended as a follow-up.

