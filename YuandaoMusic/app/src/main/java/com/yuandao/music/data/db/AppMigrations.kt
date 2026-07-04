package com.yuandao.music.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `playback_history` (
                    `trackId` TEXT NOT NULL,
                    `firstPlayedAtMs` INTEGER NOT NULL,
                    `lastPlayedAtMs` INTEGER NOT NULL,
                    `playCount` INTEGER NOT NULL,
                    PRIMARY KEY(`trackId`)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_playback_history_lastPlayedAtMs` " +
                    "ON `playback_history` (`lastPlayedAtMs`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_playback_history_playCount` " +
                    "ON `playback_history` (`playCount`)"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `library_roots` (
                    `uri` TEXT NOT NULL,
                    `displayName` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `enabled` INTEGER NOT NULL,
                    `createdAtMs` INTEGER NOT NULL,
                    `updatedAtMs` INTEGER NOT NULL,
                    `lastScannedAtMs` INTEGER,
                    PRIMARY KEY(`uri`)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_library_roots_type` " +
                    "ON `library_roots` (`type`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_library_roots_enabled` " +
                    "ON `library_roots` (`enabled`)"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
