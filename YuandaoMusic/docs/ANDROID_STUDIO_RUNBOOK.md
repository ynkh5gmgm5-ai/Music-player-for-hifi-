# Android Studio Runbook

This runbook is for running Yuandao Music from Android Studio and collecting useful evidence when
playback or scanning fails.

## Prerequisites

- JDK 17.
- Android Studio installed.
- Android SDK platform 35 installed.
- Network access for the first Gradle dependency sync.
- A small test folder with at least one WAV file and one FLAC file.

The known working JDK on this workstation is:

```text
C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
```

If PowerShell cannot find Java, set it for the current terminal:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

## Open The Project

1. Open Android Studio.
2. Choose **Open**.
3. Select `D:\我的文档\Documents\YUANDAO\YuandaoMusic`.
4. Wait for Gradle sync to finish.
5. If Android Studio asks to trust the project, trust it only if the path matches the folder above.

## Build From Terminal

From `YuandaoMusic/`:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace
```

This is the normal quality gate before a development stage is considered complete.

## First Runtime Test

1. Create or start an Android emulator, or connect an Android device.
2. Run the `app` configuration from Android Studio.
3. Grant media/audio permissions if prompted.
4. Tap the folder action and select a test folder through Android's file picker.
5. Wait for scan status to report indexed tracks.
6. Tap a FLAC track and confirm playback controls appear.
7. Tap the mini player to open the now-playing sheet.
8. Repeat with a WAV track.

## Useful Logcat Filters

Use Android Studio Logcat and start broad. Good filters to try:

```text
package:com.yuandao.music
```

If playback fails, also search the visible Logcat text for:

```text
ExoPlayer
MediaCodec
AudioTrack
MediaSession
PlaybackController
```

If scanning fails, search for:

```text
MediaStore
DocumentFile
ContentResolver
SecurityException
FileNotFoundException
```

## What To Capture For A Bug

Capture:

- Screenshot or short screen recording.
- Device or emulator model.
- Android version.
- File format and basic file details.
- Exact steps that reproduced the issue.
- Relevant Logcat lines.

Use `docs/manual-verification/runtime-issue-template.md` for the report.

## When To Stop And Ask For Help

Stop and ask before making risky workarounds if:

- Android refuses folder permission repeatedly.
- A file plays in another player but fails here.
- A crash repeats after the same action.
- Playback works in foreground but breaks in background.
- A fix would require changing the playback engine, database schema, or permission model.

