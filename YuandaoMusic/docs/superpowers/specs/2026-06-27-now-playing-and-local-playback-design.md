# Now Playing And Local Playback Design

## Goal

Build the next Android local-playback layer: a usable now-playing surface, clearer queue ordering, and a practical WAV/FLAC verification path. This stage keeps cloud, streaming, USB exclusive output, Linux, DSD, APE, and CUE out of scope.

## Design Direction

The app keeps `PlaybackUiState` as the source of truth. Compose should not rebuild playback rules directly; it should consume small derived UI models that are easy to test without Android runtime dependencies.

The first player surface will be a full-height modal sheet opened from the mini player. This avoids adding navigation before the core playback loop is stable, while still giving users a dedicated place for artwork, title, artist, album, format, quality, progress, lyrics, and playback controls.

## Components

- `NowPlayingScreenState`: a pure UI projection that combines `PlaybackUiState` and `TimedLyrics`.
- `PlaybackQueueDrawerState`: remains the queue drawer model, extended with position labels so shuffled order and upcoming order are visible.
- `HomeScreen`: owns local booleans for showing the now-playing sheet and queue drawer, then passes existing playback callbacks to both surfaces.
- `docs/manual-verification/wav-flac-local-playback.md`: records the manual verification path for scanning and playing real WAV/FLAC files once an emulator or device can access a folder.

## Behavior

- Tapping the mini player opens the now-playing sheet.
- The now-playing sheet shows a stable empty state if no track is active.
- Progress is clamped between `0f` and `1f`; unknown duration renders `0:00` and a zero progress bar.
- Lyrics show the active line at the current playback position when available, otherwise a concise empty state.
- Queue items show their order in the active queue. When shuffle is enabled, the visible order is the real shuffled playback order already held in `PlaybackUiState.queue`.
- WAV and FLAC are treated as first-pass local playback formats. DSD, APE, and CUE remain listed as recognized but deferred formats.

## Error Handling

Playback errors remain surfaced through `PlaybackUiState.errorMessage`. The now-playing sheet mirrors the same message instead of introducing a second error channel.

## Testing

Use JUnit tests for pure projection logic:

- Now-playing state with active track, progress, quality labels, and lyrics.
- Empty now-playing state.
- Clamped progress when position exceeds duration.
- Queue drawer item order labels.
- Format boundary: WAV/FLAC remain first-pass playable, DSD/APE/CUE remain deferred.

