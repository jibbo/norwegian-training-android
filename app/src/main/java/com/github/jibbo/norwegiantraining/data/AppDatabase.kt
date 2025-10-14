package com.github.jibbo.norwegiantraining.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.jibbo.norwegiantraining.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                prepopulateWorkouts()
            }
        }

        private suspend fun prepopulateWorkouts() {
            val workoutDao = workoutDaoProvider.get()
            val jsonString = context.resources.openRawResource(R.raw.workouts).bufferedReader()
                .use { it.readText() }
            val gson = Gson()
            val workoutDataType = object : TypeToken<List<WorkoutData>>() {}.type
            val workoutData: List<WorkoutData> = gson.fromJson(jsonString, workoutDataType)

            val workouts = workoutData.map { data ->
                val nameResId =
                    context.resources.getIdentifier(data.name_res_id, "string", context.packageName)
                Workout(
                    name = context.getString(nameResId),
                    difficulty = Difficulty.valueOf(data.difficulty),
                    content = data.content
                )
            }.toTypedArray()
            workoutDao.insert(*workouts)
        }
    }
}

data class WorkoutData(
    val name_res_id: String,
    val difficulty: String,
    val content: String
)

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
