# Pages and Database helper methods used

This report lists each app page under src/main/java/com/studentgui/apppages and the Database helper methods (and other helpers) each page calls when saving or refreshing.

- Abacus.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("Abacus")
  - createProgressSession(studentId, ptId, date)
  - insertAssessmentResults(sessionId, ptId, codes, scores)
  - fetchLatestAssessmentResults(studentName, "Abacus", n)
  - SessionJsonWriter.writeSessionJson(studentName, "Abacus", AssessmentPayload, sessionId)
  - JLineGraph.saveChart(...) (saves PNG)

- Braille.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("Braille")
  - createProgressSession(studentId, ptId, date)
  - insertAssessmentResults(sessionId, ptId, codes, scores)
  - fetchLatestAssessmentResults(studentName, "Braille", n)
  - SessionJsonWriter.writeSessionJson(..., "Braille", AssessmentPayload, sessionId)
  - JLineGraph.saveChart(...)

- BrailleNote.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("BrailleNote")
  - createProgressSession(...)
  - insertAssessmentResults(...)
  - SessionJsonWriter.writeSessionJson(..., "BrailleNote", AssessmentPayload, sessionId)
  - JLineGraph.saveChart(...)

- BrailleSense.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("BrailleSense")
  - createProgressSession(...)
  - insertAssessmentResults(...)
  - SessionJsonWriter.writeSessionJson(..., "BrailleSense", AssessmentPayload, sessionId)
  - JLineGraph.saveChart(...)

- ScreenReader.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("ScreenReader")
  - createProgressSession(...)
  - insertAssessmentResults(...)
  - fetchLatestAssessmentResults(studentName, "ScreenReader", n)
  - SessionJsonWriter.writeSessionJson(..., "ScreenReader", AssessmentPayload, sessionId)
  - JLineGraph.saveChart(...)

- DigitalLiteracy.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("DigitalLiteracy")
  - createProgressSession(...)
  - insertAssessmentResults(...)
  - SessionJsonWriter.writeSessionJson(..., "DigitalLiteracy", AssessmentPayload, sessionId)
  - JLineGraph.saveChart(...)

- IOS.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("iOS")
  - createProgressSession(...)
  - insertAssessmentResults(...)
  - fetchLatestAssessmentResults(studentName, "iOS", n)
  - SessionJsonWriter.writeSessionJson(..., "iOS", AssessmentPayload, sessionId)
  - JLineGraph.saveChart(...)

- CVI.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("CVI")
  - createProgressSession(...)
  - insertAssessmentResults(...)
  - SessionJsonWriter.writeSessionJson(..., "CVI", AssessmentPayload, sessionId)
  - JLineGraph.saveChart(...)

- Keyboarding.java (specialized keyboarding table)
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("Keyboarding")
  - createProgressSession(...)
  - insertKeyboardingResult(sessionId, program, topic, speed, accuracy)
  - SessionJsonWriter.writeSessionJson(..., "Keyboarding", KeyboardingPayload, sessionId)

- Observations.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("Observations")
  - createProgressSession(...)
  - insertAssessmentResults(sessionId, ptId, new String[]{"OBS_NOTE"}, new int[]{0})
  - saveSessionNotes(sessionId, notes)
  - SessionJsonWriter.writeSessionJson(..., "Observations", NotesPayload, sessionId)

- ContactLog.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("ContactLog")
  - createProgressSession(...)
  - saveSessionNotes(sessionId, notes)
  - saveContactLog(sessionId, studentName, dateString, guardian, method, phone, email, response, general, specific, notes)
  - SessionJsonWriter.writeSessionJson(..., "ContactLog", ContactPayload, sessionId)
  - fetchLatestContactLog(studentName) used on Load Last Contact

- SessionNotes.java
  - getOrCreateStudent(studentName)
  - getOrCreateProgressType("SessionNotes")
  - createProgressSession(...)
  - saveSessionNotes(sessionId, notes)
  - SessionJsonWriter.writeSessionJson(..., "SessionNotes", NotesPayload, sessionId)

- InstructionalMaterials.java
  - No DB persistence (static read-only viewer)

- Homepage (Homepage.create())
  - No DB persistence (static overview pane)

Notes

- All assessment pages that create assessment sessions call SessionJsonWriter.writeSessionJson(...) with a typed DTO (AssessmentPayload, NotesPayload, KeyboardingPayload, ContactPayload, etc.).
- The SQL schema generator (SqlGenerate) creates the tables referenced above: Student, ProgressType, ProgressSession, AssessmentPart, AssessmentResult, KeyboardingResult, ContactLog, etc.

Recent changes (applied in branch refactor/exception-cleanup)

- Braille submitData array sizing
  - The `Braille` page previously constructed fixed-size arrays when preparing
    the `codes` and `scores` to persist into the normalized schema. That could
    lead to a mismatch between stored columns and the plotted series. The code
    now allocates arrays using the actual `partCodes.length` so storage and
    plotting stay aligned.

- Default student behavior
  - Many pages now default to the first roster entry when constructed with a
    null or empty student name. The helper `com.studentgui.apphelpers.Helpers.defaultStudent()`
    returns the first entry in `json_Files/students.json` (or a sensible
    fallback) and is used by pages to avoid null student names on open.

- Plot visualization updates (JLineGraph)
  - Plotted points receive a small rendering jitter of ±0.10 so overlapping
    points are easier to identify visually. This jitter is applied at render
    time only and does not mutate persisted values.
  - Background bands have been changed to the following numeric ranges:
    red = -0.25..0.5, orange = 0.5..1.5, orange = 1.5..2.5, yellow = 2.5..3.5,
    green = 3.5..4.5. The Y-axis limits have been adjusted to -0.25..4.25.

Verification & build notes

- After applying these changes, run `mvn -DskipTests package` in the project
  root to compile the project and produce the shaded jar in `target/`.

End of report.
