# WAV/FLAC Local Playback Manual Verification

## Scope

This checklist verifies the first Android local playback path for WAV and FLAC. It does not verify USB exclusive output, Linux playback, cloud libraries, streaming services, DSD, APE, or CUE playback.

## Test Material

- Use at least one FLAC track.
- Use at least one WAV track.
- Prefer files with known sample rate and bit depth, for example `24-bit / 96kHz`.
- If using the workstation music drive, copy or expose a small WAV/FLAC folder to the Android emulator/device through a path the app can scan with SAF.

## Setup

1. Install or run the debug build from Android Studio.
2. Grant audio/media permissions when prompted.
3. Use **Add folder** to choose the test folder, or use the local scan action if the files are in device media storage.
4. Wait until the scan status reports indexed tracks.

## Playback Checks

1. Confirm the library shows the WAV and FLAC files in recent/local sections.
2. Tap a FLAC track.
3. Confirm the mini player appears and playback starts or can be started.
4. Tap the mini player to open the now-playing sheet.
5. Confirm the sheet shows:
   - title, artist, and album
   - format and source quality
   - progress and duration
   - lyrics line or the empty lyric state
   - previous, play/pause, next, shuffle, repeat, queue, and close controls
6. Seek near the middle of the track and confirm playback continues from the selected position.
7. Tap next and previous and confirm the queue position changes.
8. Enable shuffle and confirm the queue drawer shows the actual randomized order with item numbers.
9. Repeat the same checks with a WAV file.

## Background And Recovery Checks

1. Start playback.
2. Leave the app and confirm playback continues.
3. Reopen the app and confirm the mini player still reflects the current track and progress.
4. Pause playback and confirm the state changes to paused.
5. Use the close control and confirm the mini player disappears after playback is stopped.

## If Something Fails

Use `docs/manual-verification/runtime-issue-template.md` to capture the environment, file details,
steps, expected result, actual result, screenshots, and Logcat output. A precise report is more useful
than trying to guess the cause from memory.

## Expected Deferrals

- DSD, APE, and CUE may be scanned or recognized, but they are not expected to play in this phase.
- USB DAC exclusive mode is not expected in this phase.
- Cloud and streaming sources are not expected in this phase.
