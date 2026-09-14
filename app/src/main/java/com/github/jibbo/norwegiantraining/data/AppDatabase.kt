package com.github.jibbo.norwegiantraining.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.jibbo.norwegiantraining.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Database(
    entities = [
        Session::class,
        Workout::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(SessionConverters::class, WorkoutConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): SessionDao
    abstract fun workoutDao(): WorkoutDao

    class PrepopulateCallback @Inject constructor(
        @ApplicationContext private val context: Context,
        private val workoutDaoProvider: Provider<WorkoutDao>
    ) : Callback() {

        val workouts = listOf(
            Workout(
                name = context.getString(R.string.workout_first_steps),
                difficulty = Difficulty.BEGINNER,
                content = "5m-30s-15s-30s-15s-30s-15s-30s-15s-30s-15s-30s-15s-30s-15s-30s-15s-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_beginner_1),
                difficulty = Difficulty.BEGINNER,
                content = "5m-1m-30s-1m-30s-1m-30s-1m-30s-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_beginner_2),
                difficulty = Difficulty.BEGINNER,
                content = "5m-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_beginner_3),
                difficulty = Difficulty.BEGINNER,
                content = "5m-90s-1m-90s-1m-90s-1m-90s-1m-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_not_so_beginner),
                difficulty = Difficulty.BEGINNER,
                content = "5m-90s-1m-90s-1m-90s-1m-90s-1m-90s-1m-90s-1m-90s-1m-90s-1m-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_intermediate_1),
                difficulty = Difficulty.INTERMEDIATE,
                content = "5m-2m-90s-2m-90s-2m-90s-2m-90s-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_intermediate_2),
                difficulty = Difficulty.INTERMEDIATE,
                content = "5m-3m-2m-3m-2m-3m-2m-3m-2m-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_intermediate_3),
                difficulty = Difficulty.INTERMEDIATE,
                content = "5m-3m-3m-3m-3m-3m-3m-3m-3m-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_expert_1),
                difficulty = Difficulty.EXPERT,
                content = "5m-3m-3m-3m-3m-3m-3m-3m-3m-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_expert_2),
                difficulty = Difficulty.EXPERT,
                content = "5m-198s-198s-198s-198s-198s-198s-198s-198s-5m"
            ),
            Workout(
                name = context.getString(R.string.workout_true_norwegian),
                difficulty = Difficulty.EXPERT,
                content = "5m-4m-4m-4m-4m-4m-4m-4m-4m-5m"
            ),
        )

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                // The database is the source of truth; do not rely on a local flag
                // that can be missing after an upgrade or restored independently.
                if (workoutDaoProvider.get().firstWorkout() == null) {
                    prepopulateWorkouts()
                }
            }
        }

        private suspend fun prepopulateWorkouts() {
            workoutDaoProvider.get().insert(*workouts.toTypedArray())
        }
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM Session WHERE date IS NULL OR length(date) != 10")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS Session_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                phases_ended INTEGER NOT NULL,
                skip_count INTEGER NOT NULL,
                date INTEGER NOT NULL
            )
            """
        )
        db.execSQL(
            """
            INSERT INTO Session_new (id, phases_ended, skip_count, date)
            SELECT id, phases_ended, skip_count,
                CAST(strftime('%s',
                    substr(date, 7, 4) || '-' || substr(date, 4, 2) || '-' || substr(date, 1, 2)
                ) AS INTEGER) * 1000
            FROM Session
            WHERE strftime('%s',
                substr(date, 7, 4) || '-' || substr(date, 4, 2) || '-' || substr(date, 1, 2)
            ) IS NOT NULL
            """
        )
        db.execSQL("DROP TABLE Session")
        db.execSQL("ALTER TABLE Session_new RENAME TO Session")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Session ADD COLUMN is_manual INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE Session ADD COLUMN name TEXT NOT NULL DEFAULT 'HIIT'")
        db.execSQL("ALTER TABLE Session ADD COLUMN duration INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE Workout ADD COLUMN isCustom INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Session ADD COLUMN activity_type INTEGER NOT NULL DEFAULT 5")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE Session_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                phases_ended INTEGER NOT NULL,
                skip_count INTEGER NOT NULL,
                date INTEGER NOT NULL,
                is_manual INTEGER NOT NULL,
                name TEXT NOT NULL,
                duration INTEGER NOT NULL,
                activity_type TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO Session_new (
                id, phases_ended, skip_count, date, is_manual, name, duration, activity_type
            )
            SELECT
                id, phases_ended, skip_count, date, is_manual, name, duration,
                CASE activity_type
                    WHEN 0 THEN 'RUN'
                    WHEN 1 THEN 'STRENGTH_TRAINING'
                    WHEN 2 THEN 'CYCLING'
                    WHEN 3 THEN 'SWIMMING'
                    WHEN 4 THEN 'WALKING'
                    WHEN 5 THEN 'HIIT'
                    ELSE 'HIIT'
                END
            FROM Session
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE Session")
        db.execSQL("ALTER TABLE Session_new RENAME TO Session")
    }
}

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        callback: AppDatabase.PrepopulateCallback
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "norwegiantrainingdb"
    ).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).addCallback(callback).build()

    @Provides
    fun provideRecordDao(database: AppDatabase) = database.recordDao()

    @Provides
    fun provideWorkoutDao(database: AppDatabase) = database.workoutDao()
}
