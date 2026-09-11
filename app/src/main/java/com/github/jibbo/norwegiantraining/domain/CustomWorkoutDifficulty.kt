package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty

/** Calculates persisted custom-workout difficulty from exact generated duration. */
fun calculateCustomWorkoutDifficulty(validation: CustomWorkoutValidationResult): Difficulty? {
    if (!validation.isValid) return null

    val workMinutes = validation.workMinutes ?: return null
    val workSeconds = validation.workSeconds ?: return null
    val restMinutes = validation.restMinutes ?: return null
    val restSeconds = validation.restSeconds ?: return null
    val rounds = validation.rounds ?: return null

    val workDuration = workMinutes * SECONDS_PER_MINUTE + workSeconds
    val restDuration = restMinutes * SECONDS_PER_MINUTE + restSeconds
    val totalSeconds = WARM_UP_SECONDS + rounds * (workDuration + restDuration) + COOLDOWN_SECONDS

    return when {
        totalSeconds < INTERMEDIATE_MINIMUM_SECONDS -> Difficulty.BEGINNER
        totalSeconds <= EXPERT_MINIMUM_SECONDS -> Difficulty.INTERMEDIATE
        else -> Difficulty.EXPERT
    }
}

private const val SECONDS_PER_MINUTE = 60
private const val WARM_UP_SECONDS = 300
private const val COOLDOWN_SECONDS = 300
private const val INTERMEDIATE_MINIMUM_SECONDS = 25 * SECONDS_PER_MINUTE
private const val EXPERT_MINIMUM_SECONDS = 40 * SECONDS_PER_MINUTE
