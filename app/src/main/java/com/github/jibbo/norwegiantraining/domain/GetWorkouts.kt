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
            return hashMapOf(
                Difficulty.BEGINNER to listOf(
                    Workout(
                        name = "Beginner 1",
                        difficulty = Difficulty.BEGINNER,
                        content = "1-1-1-1"
                    ),
                    Workout(
                        name = "Beginner 2",
                        difficulty = Difficulty.BEGINNER,
                        content = "2-2-2-2"
                    )
                ),
                Difficulty.INTERMEDIATE to listOf(
                    Workout(
                        name = "Intermediate 1",
                        difficulty = Difficulty.INTERMEDIATE,
                        content = "3-3-3-3"
                    ),
                    Workout(
                        name = "Intermediate 2",
                        difficulty = Difficulty.INTERMEDIATE,
                        content = "4-3-4-3"
                    )
                ),
                Difficulty.EXPERT to listOf(
                    Workout(
                        name = "Advanced 1",
                        difficulty = Difficulty.EXPERT,
                        content = "4-4-4-4"
                    )
                )
            )
        }
        return workouts
    }
}
