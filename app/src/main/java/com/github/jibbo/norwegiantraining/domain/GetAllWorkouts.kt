package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllWorkouts @Inject constructor(
    private val workOutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<Map<Difficulty, List<Workout>>> {
        return workOutRepository.getAll().map { workouts ->
            workouts.groupBy { it.difficulty }
        }
    }
}
