# Architecture

Yuandao Music starts as an Android local player, but the model layer is source-aware from day one.

## Layers

- `scanner`: MediaStore and SAF discovery, Android metadata extraction, embedded cover caching.
- `data/db`: Room entities and DAO for tracks, albums, artists, playback queue, and playback state.
- `data/model`: Platform-facing music models and future source types.
- `data/repository`: Scanning orchestration and database mapping.
- `playback`: Media3-based queue playback, MediaSession service, repeat/shuffle, seek, and state persistence.
- `lyrics`: Sidecar `.lrc` parser and lookup. Embedded lyrics are reserved for the next milestone.
- `ui`: Jetpack Compose shell using the dark glass visual system from the product render, including
  the home screen, mini player, now-playing sheet, and queue drawer.

## Source Model

Tracks carry an `AudioSourceType`:

- `LOCAL`: active in milestone 1.
- `CLOUD`: reserved for the private Yuandao cloud library.
- `STREAMING`: reserved for licensed streaming providers.

The UI already exposes cloud and streaming positions, but they are intentionally inactive.

## Data Layer Status

The current data layer is strong enough for the first local-player milestone: tracks are source-aware,
Room stores local songs/albums/artists, playback queue state is persisted, and the model already has
`LOCAL`, `CLOUD`, and `STREAMING` source types.

It is not yet the final multi-source library layer. Before cloud or streaming work starts, the project
still needs source adapters, remote identity mapping, cache policy, deleted-file reconciliation,
pagination/sync state, and source-specific playback authorization. Those concerns are deliberately
kept out of the first Android local playback milestone.

## Milestones

The detailed roadmap lives in `docs/ROADMAP.md`. The short version is:

1. Verify Android local WAV/FLAC playback on a real runtime.
2. Polish local playback, queue, error handling, and local library management.
3. Refine the visual system against the product reference once the playback loop is stable.
4. Expand HiFi metadata and advanced output in separate modules.
5. Add private cloud before any licensed streaming work.

## Local Playback Boundary

The first Android build treats Media3 as the active playback engine. MP3, AAC, FLAC, WAV, ALAC,
OGG, and OPUS are first-pass playable formats. APE, DSD, and CUE are indexed as HiFi library
assets, but playback is deliberately rejected with a user-facing message until a dedicated engine
module is added.

Playback state is persisted separately from the UI: queue order, current track, position, repeat,
and shuffle can be restored after the library is available again. The queue planner is pure Kotlin
so format support, restored index selection, and deferred-format rejection can be tested without an
Android device.

`YuandaoPlaybackService` exposes the player through Media3 `MediaSessionService`, which gives Android
system surfaces a stable playback session for media buttons, lock screen controls, and media
notifications. UI code talks to `PlaybackGateway` instead of the concrete engine so the visual layer
can evolve independently from the playback service.

The local player keeps a two-way playback state loop: Media3 player events update `PlaybackUiState`,
and UI commands flow back through `PlaybackGateway`. UI surfaces consume tested projection models
such as `NowPlayingScreenState`, `PlaybackQueueSummary`, and `PlaybackQueueDrawerState` instead of
rebuilding playback rules inside Compose. Runtime audio format information is collected from
ExoPlayer analytics callbacks and merged with scanned metadata, so the UI can show playback format,
sample rate, bit depth, channel count, bitrate, and decoder name when available.

Shuffle is implemented by randomizing the actual queue while keeping the current track at the front.
This gives a no-repeat random order for the current cycle and lets the randomized queue be persisted.
The queue drawer shows the resulting order with stable position labels. The player also records
recently played tracks in Room. Audio focus is handled by the app through a small policy layer:
permanent loss pauses without resume, transient loss remembers whether playback should resume, and
duck events lower volume until focus returns. Noisy-output handling pauses playback when headphones
are unplugged. Phone-call interruption handling is intentionally not part of this milestone.

## Licensing Boundary

Auxio is an architectural reference only. Its GPL code must not be copied into this project.
