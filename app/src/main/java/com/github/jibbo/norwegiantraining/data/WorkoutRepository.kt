package com.github.jibbo.norwegiantraining.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface WorkoutRepository {
    fun getAll(): Flow<List<Workout>>
    suspend fun getByDifficulty(difficulty: Difficulty): List<Workout>
    suspend fun getById(id: Long): Workout?
    suspend fun getDifficulties(): List<Difficulty>
    suspend fun insert(vararg workouts: Workout)
    suspend fun insert(workouts: List<Workout>)
}

class PersistentWorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override suspend fun insert(workouts: List<Workout>) {
        workoutDao.insert(*workouts.toTypedArray())
    }

    override suspend fun insert(vararg workouts: Workout) {
        workoutDao.insert(*workouts)
    }

    override fun getAll(): Flow<List<Workout>> = workoutDao.getAll()

    override suspend fun getByDifficulty(difficulty: Difficulty): List<Workout> =
        workoutDao.getByDifficulty(difficulty)

    override suspend fun getById(id: Long): Workout? = workoutDao.getById(id)

    override suspend fun getDifficulties(): List<Difficulty> = Difficulty.entries.toList()

}

@Module
@InstallIn(SingletonComponent::class)
interface WorkoutRepositoryModule {
    @Binds
    @Singleton
    fun bindWorkoutRepository(
        persistentWorkoutRepository: PersistentWorkoutRepository
    ): WorkoutRepository
}
