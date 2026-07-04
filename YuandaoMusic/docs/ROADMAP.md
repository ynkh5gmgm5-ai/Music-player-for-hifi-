# Roadmap

This roadmap keeps Yuandao Music focused on a reliable local HiFi player before expanding into
cloud, streaming, Linux, or advanced output.

## Phase 0: Governance And Runtime Readiness

Goal: make the project easy to run, verify, debug, and hand off.

Current work:

- Project status matrix.
- Android Studio runbook.
- WAV/FLAC manual verification checklist.
- Runtime issue template.
- Architecture notes aligned with the current code.

Exit gate:

- `testDebugUnitTest assembleDebug lintDebug` passes.
- Android Studio can open and sync the project.
- Runtime issues can be reported with enough evidence to reproduce.

## Phase 1: Android Local Playback Verification

Goal: prove WAV/FLAC playback works on a real Android runtime.

Scope:

- MediaStore scan.
- SAF folder scan.
- WAV and FLAC playback.
- Play/pause, previous, next, seek.
- Shuffle and repeat.
- Now-playing sheet.
- Queue drawer.
- Sidecar `.lrc` display.
- Background playback and service close behavior.

Exit gate:

- At least one WAV and one FLAC file can be scanned and played.
- Queue controls work across track changes.
- Shuffle shows and follows the actual randomized queue order.
- Background playback and app resume preserve visible state.
- Runtime failures are either fixed or documented with a concrete issue report.

## Phase 2: Local Playback Polish

Goal: make the Android local player feel smooth enough for daily listening tests.

Scope:

- Queue item tap-to-play.
- Remove from queue.
- Clear queue.
- Better empty, loading, and error states.
- More useful playback errors for unsupported or inaccessible files.
- More robust recent playback history.
- Better long-title and missing-cover behavior.

Exit gate:

- A user can scan a folder, play an album, shuffle a library, recover from simple errors, and understand what the player is doing.

## Phase 3: Local Library Management

Goal: make the local library navigable instead of just playable.

Scope:

- Song, artist, album, and folder views.
- Search.
- Sorting and filtering.
- Format filters for WAV, FLAC, ALAC, MP3, and deferred HiFi formats.
- Re-scan and stale-file reconciliation.

Exit gate:

- A medium-sized library can be browsed and played without needing external file managers.

## Phase 4: Visual System And 1:1 Reference Work

Goal: turn the working player into the Yuandao visual product.

Scope:

- Component tokens for color, typography, spacing, radius, and glass panels.
- Home screen visual refinement against the reference render.
- Now-playing page refinement.
- Queue and library page visual consistency.
- Android responsive behavior for phone/tablet shapes.

Exit gate:

- Core screens follow one visual standard and can be compared against the reference render without rebuilding functional logic.

## Phase 5: HiFi Metadata Expansion

Goal: improve the accuracy and richness of local music metadata.

Scope:

- Embedded lyrics.
- Richer tag reading.
- Better bit-depth handling.
- More cover edge cases.
- APE/CUE research and possible staged support.
- DSD remains separate until the engine/output strategy is clear.

Exit gate:

- Metadata shown in the UI is trustworthy for common WAV/FLAC libraries.

## Phase 6: Advanced Output

Goal: handle serious HiFi output requirements without destabilizing normal playback.

Scope:

- USB DAC discovery and output state.
- Exclusive output research.
- Sample-rate routing strategy.
- Device-specific behavior notes.
- DSD/DoP research.

Exit gate:

- The app has a separate, well-tested output module instead of mixing exclusive-output logic into normal playback paths.

## Phase 7: Private Cloud Library

Goal: add the user's own remote library before commercial streaming.

Scope:

- Source adapter interface.
- Remote file identity.
- Auth/session model.
- Sync state.
- Cache policy.
- Remote playback or progressive download strategy.

Exit gate:

- Local and cloud tracks can share UI concepts without pretending they have the same storage or permission model.

## Phase 8: Streaming And Linux

Goal: expand only after Android local and private cloud foundations are stable.

Streaming concerns:

- Licensing.
- Provider agreements.
- API availability.
- DRM or token restrictions.
- Public relations and brand constraints.

Linux concerns:

- Shared domain model.
- Platform-specific playback engine.
- Desktop library access.
- Output device routing.

Exit gate:

- These become separate specs and implementation plans instead of being mixed into Android local playback.

## User Help Points

The user can help most by providing:

- Runtime screenshots or screen recordings.
- Android Studio Logcat excerpts.
- WAV/FLAC sample files that reproduce bugs.
- Confirmation on product behavior when trade-offs are real.
- Figma/reference details when the visual-system phase starts.

