package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import javax.inject.Inject

class GetWorkouts @Inject constructor(
    private val workOutRepository: WorkoutRepository
) {
    suspend operator fun invoke() = workOutRepository.getAll()
}
