package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import javax.inject.Inject

class GetWorkouts @Inject constructor(
    private val workOutRepository: WorkoutRepository
) {
    suspend operator fun invoke(): HashMap<Difficulty, List<Workout>> {
        val workouts = workOutRepository.getAll()
        if (workouts.isEmpty()) {
            workOutRepository.insert(basicWorkouts.values.flatten())
            return basicWorkouts
        }
        return workouts
    }

    companion object {
        private val basicWorkouts = hashMapOf(
            Difficulty.BEGINNER to listOf(
                Workout(
                    name = "Beginner 1",
                    difficulty = Difficulty.BEGINNER,
                    content = "10m-30s-15s-30s-15s-30s-15s-30s-15s-10m"
                ),
                Workout(
                    name = "Beginner 2",
                    difficulty = Difficulty.BEGINNER,
                    content = "10m-1m-30s-1m-30s-1m-30s-1m-30s-10m"
                )
            ),
            Difficulty.INTERMEDIATE to listOf(
                Workout(
                    name = "Intermediate 1",
                    difficulty = Difficulty.INTERMEDIATE,
                    content = "10m-3m-3m-3m-3m-3m-3m-3m-3m-10m"
                ),
            ),
            Difficulty.EXPERT to listOf(
                Workout(
                    name = "Expert 1",
                    difficulty = Difficulty.EXPERT,
                    content = "10m-4m-4m-4m-4m-4m-4m-4m-4m-10m"
                )
            )
        )
    }
}
