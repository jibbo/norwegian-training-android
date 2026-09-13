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
    fun getCustomWorkouts(): Flow<List<Workout>>
    fun getBuiltInWorkouts(): Flow<List<Workout>>
    suspend fun getByDifficulty(difficulty: Difficulty): List<Workout>
    suspend fun getById(id: Long): Workout?
    suspend fun getDifficulties(): List<Difficulty>
    suspend fun insertCustom(workout: Workout): Long
    suspend fun updateCustom(workout: Workout): Boolean
    suspend fun deleteCustom(id: Long): Boolean
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

    override fun getCustomWorkouts(): Flow<List<Workout>> = workoutDao.getCustomWorkouts()

    override fun getBuiltInWorkouts(): Flow<List<Workout>> = workoutDao.getBuiltInWorkouts()

    override suspend fun getByDifficulty(difficulty: Difficulty): List<Workout> =
        workoutDao.getByDifficulty(difficulty)

    override suspend fun getById(id: Long): Workout? = workoutDao.getById(id)

    override suspend fun getDifficulties(): List<Difficulty> = Difficulty.entries.toList()

    override suspend fun insertCustom(workout: Workout): Long = workoutDao.insertCustom(workout)

    override suspend fun updateCustom(workout: Workout): Boolean =
        workoutDao.updateCustomById(
            id = workout.id,
            name = workout.name,
            difficulty = workout.difficulty,
            content = workout.content,
            icon = workout.icon,
        ) == 1

    override suspend fun deleteCustom(id: Long): Boolean = workoutDao.deleteCustomById(id) == 1

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
