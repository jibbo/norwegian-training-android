package com.github.jibbo.norwegiantraining.data

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseSessionMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private var database: AppDatabase? = null
    private var databaseName: String? = null

    @After
    fun tearDown() {
        database?.close()
        databaseName?.let { context.deleteDatabase(it) }
    }

    @Test
    fun migratesSessionMetadataFromVersionThree() {
        val name = "session_migration_${System.nanoTime()}"
        databaseName = name
        createVersionThreeDatabase(name)

        database = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .build()

        val session = runBlocking { database!!.recordDao().getAll().single() }

        assertEquals(7, session.phasesEnded)
        assertEquals(2, session.skipCount)
        assertEquals(1234L, session.date.time)
        assertFalse(session.isManual)
        assertEquals("HIIT", session.name)
        assertEquals(0L, session.duration)
    }

    private fun createVersionThreeDatabase(name: String) {
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        val sqlite = SQLiteDatabase.openOrCreateDatabase(file, null)
        sqlite.execSQL(
            "CREATE TABLE Session (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "phases_ended INTEGER NOT NULL, " +
                "skip_count INTEGER NOT NULL, " +
                "date INTEGER NOT NULL)",
        )
        sqlite.execSQL(
            "CREATE TABLE Workout (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "difficulty INTEGER NOT NULL, " +
                "content TEXT NOT NULL)",
        )
        sqlite.execSQL(
            "CREATE TABLE room_master_table (" +
                "id INTEGER PRIMARY KEY, identity_hash TEXT)",
        )
        sqlite.execSQL(
            "INSERT INTO room_master_table (id, identity_hash) VALUES (42, '392d59f2f209f67f6c0c126887dea048')",
        )
        sqlite.execSQL(
            "INSERT INTO Session (id, phases_ended, skip_count, date) VALUES (7, 7, 2, 1234)",
        )
        sqlite.execSQL("PRAGMA user_version = 3")
        sqlite.close()
    }
}
