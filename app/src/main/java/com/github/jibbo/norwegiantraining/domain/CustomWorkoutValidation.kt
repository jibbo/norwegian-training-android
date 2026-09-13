package com.github.jibbo.norwegiantraining.domain

/** Fields that can have custom-workout validation errors. */
enum class CustomWorkoutField {
    NAME,
    WORK_MINUTES,
    WORK_SECONDS,
    WORK_DURATION,
    REST_MINUTES,
    REST_SECONDS,
    REST_DURATION,
    ROUNDS,
}

/** Validation failures are intentionally independent of localized UI strings. */
enum class CustomWorkoutValidationError {
    INVALID_NUMBER,
    OUT_OF_RANGE,
    NAME_TOO_LONG,
    ZERO_DURATION,
}

data class CustomWorkoutValidationResult(
    val trimmedName: String,
    val icon: String?,
    val workMinutes: Int?,
    val workSeconds: Int?,
    val restMinutes: Int?,
    val restSeconds: Int?,
    val rounds: Int?,
    val errors: Map<CustomWorkoutField, CustomWorkoutValidationError>,
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}

fun validateCustomWorkoutDraft(draft: CustomWorkoutDraft): CustomWorkoutValidationResult {
    val trimmedName = draft.name.trim()
    val errors = linkedMapOf<CustomWorkoutField, CustomWorkoutValidationError>()

    if (trimmedName.length > MAX_CUSTOM_WORKOUT_NAME_LENGTH) {
        errors[CustomWorkoutField.NAME] = CustomWorkoutValidationError.NAME_TOO_LONG
    }

    val workMinutes = parseDurationPart(draft.workMinutes, CustomWorkoutField.WORK_MINUTES, errors)
    val workSeconds = parseDurationPart(draft.workSeconds, CustomWorkoutField.WORK_SECONDS, errors)
    val restMinutes = parseDurationPart(draft.restMinutes, CustomWorkoutField.REST_MINUTES, errors)
    val restSeconds = parseDurationPart(draft.restSeconds, CustomWorkoutField.REST_SECONDS, errors)
    val rounds = parseRounds(draft.rounds, errors)

    if (workMinutes != null && workSeconds != null && workMinutes == 0 && workSeconds == 0) {
        errors[CustomWorkoutField.WORK_DURATION] = CustomWorkoutValidationError.ZERO_DURATION
    }
    if (restMinutes != null && restSeconds != null && restMinutes == 0 && restSeconds == 0) {
        errors[CustomWorkoutField.REST_DURATION] = CustomWorkoutValidationError.ZERO_DURATION
    }

    return CustomWorkoutValidationResult(
        trimmedName = trimmedName,
        icon = draft.icon,
        workMinutes = workMinutes,
        workSeconds = workSeconds,
        restMinutes = restMinutes,
        restSeconds = restSeconds,
        rounds = rounds,
        errors = errors,
    )
}

private fun parseDurationPart(
    value: String,
    field: CustomWorkoutField,
    errors: MutableMap<CustomWorkoutField, CustomWorkoutValidationError>,
): Int? {
    val parsed = value.trim().toIntOrNull()
    if (parsed == null) {
        errors[field] = CustomWorkoutValidationError.INVALID_NUMBER
        return null
    }
    if (parsed !in 0..59) {
        errors[field] = CustomWorkoutValidationError.OUT_OF_RANGE
        return null
    }
    return parsed
}

private fun parseRounds(
    value: String,
    errors: MutableMap<CustomWorkoutField, CustomWorkoutValidationError>,
): Int? {
    val parsed = value.trim().toIntOrNull()
    if (parsed == null) {
        errors[CustomWorkoutField.ROUNDS] = CustomWorkoutValidationError.INVALID_NUMBER
        return null
    }
    if (parsed !in 1..99) {
        errors[CustomWorkoutField.ROUNDS] = CustomWorkoutValidationError.OUT_OF_RANGE
        return null
    }
    return parsed
}

private const val MAX_CUSTOM_WORKOUT_NAME_LENGTH = 20
