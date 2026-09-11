package com.github.jibbo.norwegiantraining.home

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout

/**
 * The single workout ordering used by Home in every orientation.
 */
data class HomeWorkoutProjection(
    val recommendedBuiltIn: Workout?,
    val customWorkouts: List<Workout>,
    val remainingBuiltIns: List<Workout>,
) {
    val yourWorkouts: List<Workout>
        get() = listOfNotNull(recommendedBuiltIn) + customWorkouts

    val allBuiltIns: List<Workout>
        get() = listOfNotNull(recommendedBuiltIn) + remainingBuiltIns
}

fun Map<Difficulty, List<Workout>>.toHomeWorkoutProjection(
    recommendedWorkoutId: Long?,
): HomeWorkoutProjection {
    val workouts = values.flatten()
    val recommended = workouts.firstOrNull { workout ->
        !workout.isCustom && workout.id == recommendedWorkoutId
    }
    val custom = workouts
        .filter { it.isCustom }
        .sortedByDescending { it.id }
    val builtIns = workouts
        .filter { !it.isCustom && it.id != recommended?.id }
        .sortedBy { it.id }

    return HomeWorkoutProjection(
        recommendedBuiltIn = recommended,
        customWorkouts = custom,
        remainingBuiltIns = builtIns,
    )
}
