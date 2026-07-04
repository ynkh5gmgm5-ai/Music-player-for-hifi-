package com.yuandao.music.core

import android.content.Context
import androidx.room.Room
import com.yuandao.music.data.db.AppDatabase
import com.yuandao.music.data.db.AppMigrations
import com.yuandao.music.data.repository.MusicRepository
import com.yuandao.music.lyrics.LyricsRepository
import com.yuandao.music.playback.OutputDeviceManager
import com.yuandao.music.playback.PlaybackController
import com.yuandao.music.scanner.CoverStore
import com.yuandao.music.scanner.DocumentTreeAudioScanner
import com.yuandao.music.scanner.MediaStoreAudioScanner
import com.yuandao.music.scanner.TrackMetadataReader

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "yuandao_music.db",
    )
        .addMigrations(*AppMigrations.ALL)
        .build()

    private val coverStore = CoverStore(appContext)
    private val metadataReader = TrackMetadataReader(appContext, coverStore)
    private val mediaStoreScanner = MediaStoreAudioScanner(appContext, metadataReader)
    private val safScanner = DocumentTreeAudioScanner(appContext, metadataReader)

    val musicRepository = MusicRepository(
        dao = database.musicDao(),
        mediaStoreScanner = mediaStoreScanner,
        safScanner = safScanner,
    )

    val playbackController = PlaybackController(appContext, database.musicDao())
    val lyricsRepository = LyricsRepository(appContext)
    val outputDeviceManager = OutputDeviceManager(appContext)
}
