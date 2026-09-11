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

data class ParsedWorkoutContent(
    val workMinutes: String,
    val workSeconds: String,
    val restMinutes: String,
    val restSeconds: String,
    val rounds: String,
)

/** Parses the persisted warmup, repeated work/rest pairs, and cooldown format. */
fun parseCustomWorkoutContent(content: String): ParsedWorkoutContent? {
    val phases = content.split("-")
    if (phases.size < 4 || (phases.size - 2) % 2 != 0) return null
    val durations = phases.map { parseDuration(it) ?: return null }
    val work = durations[1]
    val rest = durations[2]
    if (durations.drop(1).dropLast(1).chunked(2).any { it[0] != work || it[1] != rest }) return null

    fun minutes(seconds: Int) = (seconds / 60).toString()
    fun remainder(seconds: Int) = (seconds % 60).toString()
    return ParsedWorkoutContent(
        workMinutes = minutes(work),
        workSeconds = remainder(work),
        restMinutes = minutes(rest),
        restSeconds = remainder(rest),
        rounds = ((phases.size - 2) / 2).toString(),
    )
}

private fun parseDuration(value: String): Int? {
    if (value.length < 2) return null
    val amount = value.dropLast(1).toIntOrNull() ?: return null
    if (amount <= 0) return null
    return when (value.last()) {
        's' -> amount
        'm' -> amount.takeIf { it <= Int.MAX_VALUE / SECONDS_PER_MINUTE }
            ?.times(SECONDS_PER_MINUTE)
        else -> null
    }
}
