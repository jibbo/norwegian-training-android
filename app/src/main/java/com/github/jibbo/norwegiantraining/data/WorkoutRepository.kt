package com.github.jibbo.norwegiantraining.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface WorkoutRepository {
    suspend fun getAll(): HashMap<Difficulty, List<Workout>>
    suspend fun getByDifficulty(difficulty: Difficulty): List<Workout>
    suspend fun getById(id: Long): Workout?
    suspend fun getDifficulties(): List<Difficulty>
}

class PersistentWorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override suspend fun getAll(): HashMap<Difficulty, List<Workout>> {
        val raw = workoutDao.getAll()
        val workouts = HashMap<Difficulty, List<Workout>>()
        for (difficulty in Difficulty.entries) {
            workouts[difficulty] = raw.filter { it.difficulty == difficulty }
        }
        return workouts
    }

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
