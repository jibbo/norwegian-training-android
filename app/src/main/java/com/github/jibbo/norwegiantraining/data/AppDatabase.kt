package com.github.jibbo.norwegiantraining.data

import android.app.Application
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [
        Session::class,
        Workout::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): SessionDao
    abstract fun workoutDao(): WorkoutDao
}

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(application: Application): AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "norwegiantrainingdb"
    ).build()

    @Provides
    fun provideRecordDao(database: AppDatabase) = database.recordDao()

    @Provides
    fun provideWorkoutDao(database: AppDatabase) = database.workoutDao()
}
