package com.github.jibbo.norwegiantraining.domain

/**
 * Generates the persisted phase sequence for a validated custom workout.
 * Returns null when the supplied validation result still contains errors.
 */
fun generateCustomWorkoutContent(validation: CustomWorkoutValidationResult): String? {
    if (!validation.isValid) return null

    val workMinutes = validation.workMinutes ?: return null
    val workSeconds = validation.workSeconds ?: return null
    val restMinutes = validation.restMinutes ?: return null
    val restSeconds = validation.restSeconds ?: return null
    val rounds = validation.rounds ?: return null

    val workDuration = workMinutes * SECONDS_PER_MINUTE + workSeconds
    val restDuration = restMinutes * SECONDS_PER_MINUTE + restSeconds
    if (workDuration == 0 || restDuration == 0) return null

    return buildList {
        add(formatDuration(WARM_UP_SECONDS))
        repeat(rounds) {
            add(formatDuration(workDuration))
            add(formatDuration(restDuration))
        }
        add(formatDuration(COOLDOWN_SECONDS))
    }.joinToString("-")
}

private fun formatDuration(totalSeconds: Int): String {
    return if (totalSeconds % SECONDS_PER_MINUTE == 0) {
        "${totalSeconds / SECONDS_PER_MINUTE}m"
    } else {
        "${totalSeconds}s"
    }
}

private const val SECONDS_PER_MINUTE = 60
private const val WARM_UP_SECONDS = 300
private const val COOLDOWN_SECONDS = 300
