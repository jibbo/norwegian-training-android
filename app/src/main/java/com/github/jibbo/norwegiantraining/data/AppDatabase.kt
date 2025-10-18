package com.github.jibbo.norwegiantraining.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 2,
    exportSchema = true
)
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
                name = context.getString(R.string.workout_true_norwegian),
                difficulty = Difficulty.EXPERT,
                content = "5m-4m-4m-4m-4m-4m-4m-4m-4m-5m"
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
            )
        )

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                prepopulateWorkouts()
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                if (workoutDaoProvider.get().syncGetAll().isEmpty())
                    prepopulateWorkouts()
            }
        }

        private suspend fun prepopulateWorkouts() {
            workoutDaoProvider.get().insert(*workouts.toTypedArray())
        }
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
    ).addCallback(callback).build()

    @Provides
    fun provideRecordDao(database: AppDatabase) = database.recordDao()

    @Provides
    fun provideWorkoutDao(database: AppDatabase) = database.workoutDao()
}
