package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session

private const val BODY_WEIGHT_KG = 70.0

private val metValues = mapOf(
    ManualWorkoutType.RUN to 9.8,
    ManualWorkoutType.STRENGTH_TRAINING to 3.5,
    ManualWorkoutType.CYCLING to 7.5,
    ManualWorkoutType.SWIMMING to 6.0,
    ManualWorkoutType.WALKING to 3.5,
    ManualWorkoutType.HIIT to 8.0,
)

fun calculateCalories(type: ManualWorkoutType, durationMinutes: Long): Double =
    metValues.getValue(type) * BODY_WEIGHT_KG * durationMinutes / 60.0

fun calculateTotalCalories(sessions: Iterable<Session>): Double =
    sessions.sumOf { calculateCalories(it.activityType.toManualWorkoutType(), it.duration) }

fun String.toManualWorkoutType(): ManualWorkoutType =
    ManualWorkoutType.entries.firstOrNull { it.name == this } ?: ManualWorkoutType.HIIT
