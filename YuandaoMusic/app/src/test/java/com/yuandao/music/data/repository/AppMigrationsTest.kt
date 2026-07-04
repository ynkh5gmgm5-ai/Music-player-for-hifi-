package com.yuandao.music.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import java.lang.reflect.Proxy
import org.junit.Assert.assertTrue
import org.junit.Test

class AppMigrationsTest {
    @Test
    fun migrationOneToTwoCreatesPlaybackHistoryTableAndIndexes() {
        val statements = mutableListOf<String>()
        val db = recordingDatabase(statements)

        AppMigrations.MIGRATION_1_2.migrate(db)

        assertTrue(statements.any { it.contains("CREATE TABLE IF NOT EXISTS `playback_history`") })
        assertTrue(statements.any { it.contains("index_playback_history_lastPlayedAtMs") })
        assertTrue(statements.any { it.contains("index_playback_history_playCount") })
    }

    @Test
    fun migrationTwoToThreeCreatesLibraryRootsTableAndIndexes() {
        val statements = mutableListOf<String>()
        val db = recordingDatabase(statements)

        AppMigrations.MIGRATION_2_3.migrate(db)

        assertTrue(statements.any { it.contains("CREATE TABLE IF NOT EXISTS `library_roots`") })
        assertTrue(statements.any { it.contains("`uri` TEXT NOT NULL") })
        assertTrue(statements.any { it.contains("`enabled` INTEGER NOT NULL") })
        assertTrue(statements.any { it.contains("index_library_roots_type") })
        assertTrue(statements.any { it.contains("index_library_roots_enabled") })
    }

    private fun recordingDatabase(statements: MutableList<String>): SupportSQLiteDatabase =
        Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            when (method.name) {
                "execSQL" -> {
                    statements += args?.firstOrNull() as String
                    Unit
                }
                else -> error("Unexpected database call: ${method.name}")
            }
        } as SupportSQLiteDatabase
}
