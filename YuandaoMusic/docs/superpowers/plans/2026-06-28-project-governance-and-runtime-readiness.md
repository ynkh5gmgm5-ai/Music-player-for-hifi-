# Project Governance And Runtime Readiness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the current Android project easier to run, verify, debug, and plan before real WAV/FLAC playback testing starts.

**Architecture:** Keep this phase documentation-only except for correcting stale architecture notes. Do not modify playback behavior. Add stable project status, Android Studio runbook, and issue-report templates so future runtime bugs can be captured without losing context.

**Tech Stack:** Markdown project docs, existing Gradle verification.

---

### Task 1: Project Status Matrix

**Files:**
- Create: `docs/PROJECT_STATUS.md`

- [ ] Document the current verified state of the Android app.
- [ ] Separate completed, partially complete, deferred, and blocked-by-runtime-verification items.
- [ ] Include the exact verification command used by maintainers.
- [ ] Record when the user needs to help, such as Android Studio runtime logs, device screenshots, or sample files.

### Task 2: Android Studio Runbook

**Files:**
- Create: `docs/ANDROID_STUDIO_RUNBOOK.md`
- Modify: `README.md`

- [ ] Document required JDK/Android SDK versions.
- [ ] Document how to open the project from Android Studio.
- [ ] Document first run, folder selection, permissions, and WAV/FLAC playback flow.
- [ ] Document how to collect Logcat evidence for playback, scanner, and Media3 issues.
- [ ] Link the runbook from the README.

### Task 3: Runtime Issue Template

**Files:**
- Create: `docs/manual-verification/runtime-issue-template.md`
- Modify: `docs/manual-verification/wav-flac-local-playback.md`

- [ ] Add a reusable issue report template for runtime bugs.
- [ ] Add the template link to the WAV/FLAC checklist.
- [ ] Include fields for device/emulator, Android version, file format, reproduction steps, expected result, actual result, screenshots, and logs.

### Task 4: Architecture And Roadmap Corrections

**Files:**
- Modify: `docs/ARCHITECTURE.md`
- Modify: `README.md`

- [ ] Correct stale audio-focus wording: the app now owns manual focus policy while Media3 handles playback.
- [ ] Mention the now-playing sheet and queue order work.
- [ ] Keep cloud, streaming, USB exclusive output, Linux, DSD, APE, and CUE clearly deferred.
- [ ] Keep the Auxio licensing boundary explicit.

### Task 5: Verification

**Files:**
- Review changed docs and project entry points.

- [ ] Run `./gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`.
- [ ] Confirm no stale placeholder markers remain in the new docs.
- [ ] Report the completed governance assets and the next runtime-verification stage.

