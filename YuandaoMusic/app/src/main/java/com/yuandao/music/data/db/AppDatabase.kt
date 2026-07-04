package com.yuandao.music.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.yuandao.music.data.model.AudioFormat
import com.yuandao.music.data.model.AudioSourceType

@Database(
    entities = [
        TrackEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaybackStateEntity::class,
        QueueItemEntity::class,
        PlaybackHistoryEntity::class,
        LibraryRootEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}

class AppConverters {
    @TypeConverter
    fun audioFormatToString(value: AudioFormat): String = value.name

    @TypeConverter
    fun audioFormatFromString(value: String): AudioFormat =
        runCatching { AudioFormat.valueOf(value) }.getOrDefault(AudioFormat.UNKNOWN)

    @TypeConverter
    fun sourceTypeToString(value: AudioSourceType): String = value.name

    @TypeConverter
    fun sourceTypeFromString(value: String): AudioSourceType =
        runCatching { AudioSourceType.valueOf(value) }.getOrDefault(AudioSourceType.LOCAL)
}
