package com.github.jibbo.norwegiantraining.data

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @Test
    fun migrate3To4PreservesRowsAndAddsCustomDefaults() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(TEST_DATABASE)
                .callback(Version3Callback())
                .build(),
        )
        val database = helper.writableDatabase

        try {
            database.execSQL(
                "INSERT INTO Workout (id, name, difficulty, content) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(7L, "Built-in", 0, "5m-30s-5m"),
            )
            database.execSQL(
                "INSERT INTO Workout (id, name, difficulty, content) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(12L, "Another", 2, "5m-1m-1m-5m"),
            )
            database.execSQL(
                "INSERT INTO Session (id, phases_ended, skip_count, date) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(3L, 8, 1, 1_700_000_000_000L),
            )

            MIGRATION_3_4.migrate(database)

            database.query("SELECT COUNT(*) FROM Workout").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(2, cursor.getInt(0))
            }
            database.query("SELECT id, name, difficulty, content, isCustom, icon FROM Workout ORDER BY id")
                .use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(7L, cursor.getLong(0))
                    assertEquals("Built-in", cursor.getString(1))
                    assertEquals(0, cursor.getInt(2))
                    assertEquals("5m-30s-5m", cursor.getString(3))
                    assertFalse(cursor.getInt(4) != 0)
                    assertNull(cursor.getString(5))

                    assertTrue(cursor.moveToNext())
                    assertEquals(12L, cursor.getLong(0))
                    assertEquals("Another", cursor.getString(1))
                    assertEquals(2, cursor.getInt(2))
                    assertEquals("5m-1m-1m-5m", cursor.getString(3))
                    assertFalse(cursor.getInt(4) != 0)
                    assertNull(cursor.getString(5))
                    assertFalse(cursor.moveToNext())
                }
            database.query("SELECT phases_ended, skip_count, date FROM Session WHERE id = 3")
                .use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(8, cursor.getInt(0))
                    assertEquals(1, cursor.getInt(1))
                    assertEquals(1_700_000_000_000L, cursor.getLong(2))
                }
        } finally {
            database.close()
            helper.close()
            context.deleteDatabase(TEST_DATABASE)
        }
    }

    private class Version3Callback : SupportSQLiteOpenHelper.Callback(3) {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE Workout (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    difficulty INTEGER NOT NULL,
                    content TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE Session (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    phases_ended INTEGER NOT NULL,
                    skip_count INTEGER NOT NULL,
                    date INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
