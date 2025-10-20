package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import javax.inject.Inject

class GetWorkoutName @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(id: Long): String? = workoutRepository.getById(id)?.name
}
