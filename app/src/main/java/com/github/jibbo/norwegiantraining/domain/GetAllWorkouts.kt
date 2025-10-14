package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import javax.inject.Inject

class GetAllWorkouts @Inject constructor(
    private val workOutRepository: WorkoutRepository
) {
    suspend operator fun invoke(): Map<Difficulty, List<Workout>> =
        workOutRepository.getAll().groupBy { it.difficulty }
}
