package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import javax.inject.Inject

class GetAllWorkouts @Inject constructor(
    private val workOutRepository: WorkoutRepository
) {
    suspend operator fun invoke(): HashMap<Difficulty, List<Workout>> {
        val workouts = workOutRepository.getAll()
        if (workouts.isEmpty()) {
            workOutRepository.insert(basicWorkouts.values.flatten())
            return basicWorkouts
        }

        val out = hashMapOf<Difficulty, List<Workout>>()
        for (difficulty in Difficulty.entries) {
            out[difficulty] = workouts.filter { it.difficulty == difficulty }
        }
        return out
    }

    companion object {
        // TODO move to firebase?
        val basicWorkouts = hashMapOf(
            Difficulty.BEGINNER to listOf(
                Workout(
                    name = "First steps",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-30s-15s-30s-15s-30s-15s-30s-15s-30s-15s-30s-15s-30s-15s-30s-15s-5m"
                ),
                Workout(
                    name = "Beginner 1",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-1m-30s-1m-30s-1m-30s-1m-30s-5m"
                ),
                Workout(
                    name = "Beginner 2",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-5m"
                ),
                Workout(
                    name = "Beginner 3",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-90s-1m-90s-1m-90s-1m-90s-1m-5m"
                ),
                Workout(
                    name = "Not so beginner",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-90s-1m-90s-1m-90s-1m-90s-1m-90s-1m-90s-1m-90s-1m-90s-1m-5m"
                )
            ),
            Difficulty.INTERMEDIATE to listOf(
                Workout(
                    name = "Intermediate 1",
                    difficulty = Difficulty.INTERMEDIATE,
                    content = "5m-2m-90s-2m-90s-2m-90s-2m-90s-5m"
                ),
                Workout(
                    name = "Intermediate 2",
                    difficulty = Difficulty.INTERMEDIATE,
                    content = "5m-3m-2m-3m-2m-3m-2m-3m-2m-5m"
                ),
                Workout(
                    name = "Intermediate 3",
                    difficulty = Difficulty.INTERMEDIATE,
                    content = "5m-3m-3m-3m-3m-3m-3m-3m-3m-5m"
                ),
            ),
            Difficulty.EXPERT to listOf(
                Workout(
                    name = "Expert 1",
                    difficulty = Difficulty.EXPERT,
                    content = "5m-3m-3m-3m-3m-3m-3m-3m-3m-5m"
                ),
                Workout(
                    name = "Expert 2",
                    difficulty = Difficulty.EXPERT,
                    content = "5m-198s-198s-198s-198s-198s-198s-198s-198s-5m"
                ),
                Workout(
                    name = "The true Norwegian",
                    difficulty = Difficulty.EXPERT,
                    content = "5m-4m-4m-4m-4m-4m-4m-4m-4m-5m"
                )
            )
        )
    }
}
