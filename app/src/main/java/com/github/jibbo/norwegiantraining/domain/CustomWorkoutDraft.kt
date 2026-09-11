package com.github.jibbo.norwegiantraining.domain

const val DEFAULT_CUSTOM_WORKOUT_ICON = "👟"

/**
 * User-entered values for creating or editing a custom workout.
 *
 * Numeric values remain strings so the form can retain incomplete or invalid input
 * until domain validation runs.
 */
data class CustomWorkoutDraft(
    val name: String = "",
    val icon: String? = DEFAULT_CUSTOM_WORKOUT_ICON,
    val workMinutes: String = "10",
    val workSeconds: String = "0",
    val restMinutes: String = "5",
    val restSeconds: String = "0",
    val rounds: String = "8",
)
