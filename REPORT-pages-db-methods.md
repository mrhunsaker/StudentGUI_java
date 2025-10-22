Pages and Database helper methods used

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

End of report.
