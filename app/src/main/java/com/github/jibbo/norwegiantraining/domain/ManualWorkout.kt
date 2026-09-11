package com.github.jibbo.norwegiantraining.domain

import java.time.LocalDate

/** The fixed product order for manually logged workout types. */
enum class ManualWorkoutType {
    RUN,
    STRENGTH_TRAINING,
    CYCLING,
    SWIMMING,
    WALKING,
    HIIT,
}

data class ManualWorkoutDraft(
    val type: ManualWorkoutType? = null,
    val date: LocalDate = LocalDate.now(),
    val hours: String = "01",
    val minutes: String = "00",
)

enum class ManualWorkoutField {
    TYPE,
    DATE,
    HOURS,
    MINUTES,
}

enum class ManualWorkoutValidationError {
    TYPE_REQUIRED,
    DATE_OUT_OF_RANGE,
    HOURS_FORMAT,
    HOURS_OUT_OF_RANGE,
    MINUTES_FORMAT,
    MINUTES_OUT_OF_RANGE,
    DURATION_ZERO,
    DURATION_TOO_LONG,
}

data class ValidatedManualWorkout(
    val type: ManualWorkoutType,
    val date: LocalDate,
    val durationMinutes: Long,
)

data class ManualWorkoutValidationResult(
    val value: ValidatedManualWorkout?,
    val errors: Map<ManualWorkoutField, ManualWorkoutValidationError>,
) {
    val isValid: Boolean
        get() = value != null && errors.isEmpty()
}

fun validateManualWorkoutDraft(
    draft: ManualWorkoutDraft,
    today: LocalDate = LocalDate.now(),
): ManualWorkoutValidationResult {
    val errors = linkedMapOf<ManualWorkoutField, ManualWorkoutValidationError>()

    val type = draft.type ?: run {
        errors[ManualWorkoutField.TYPE] = ManualWorkoutValidationError.TYPE_REQUIRED
        null
    }

    val firstDayOfYear = LocalDate.of(today.year, 1, 1)
    if (draft.date.isBefore(firstDayOfYear) || draft.date.isAfter(today)) {
        errors[ManualWorkoutField.DATE] = ManualWorkoutValidationError.DATE_OUT_OF_RANGE
    }

    val hours = parseTwoDigitValue(
        value = draft.hours,
        formatError = ManualWorkoutValidationError.HOURS_FORMAT,
        rangeError = ManualWorkoutValidationError.HOURS_OUT_OF_RANGE,
        range = 0..24,
        field = ManualWorkoutField.HOURS,
        errors = errors,
    )
    val minutes = parseTwoDigitValue(
        value = draft.minutes,
        formatError = ManualWorkoutValidationError.MINUTES_FORMAT,
        rangeError = ManualWorkoutValidationError.MINUTES_OUT_OF_RANGE,
        range = 0..59,
        field = ManualWorkoutField.MINUTES,
        errors = errors,
    )

    val durationMinutes = if (hours != null && minutes != null) {
        hours.toLong() * 60L + minutes
    } else {
        null
    }

    if (durationMinutes != null) {
        when {
            durationMinutes == 0L -> {
                errors[ManualWorkoutField.MINUTES] = ManualWorkoutValidationError.DURATION_ZERO
            }
            durationMinutes > 24L * 60L -> {
                errors[ManualWorkoutField.HOURS] = ManualWorkoutValidationError.DURATION_TOO_LONG
            }
        }
    }

    val validated = if (type != null && errors.isEmpty()) {
        ValidatedManualWorkout(type, draft.date, durationMinutes!!)
    } else {
        null
    }

    return ManualWorkoutValidationResult(validated, errors)
}

private fun parseTwoDigitValue(
    value: String,
    formatError: ManualWorkoutValidationError,
    rangeError: ManualWorkoutValidationError,
    range: IntRange,
    field: ManualWorkoutField,
    errors: MutableMap<ManualWorkoutField, ManualWorkoutValidationError>,
): Int? {
    if (!value.matches(Regex("\\d{2}"))) {
        errors[field] = formatError
        return null
    }

    val parsed = value.toInt()
    if (parsed !in range) {
        errors[field] = rangeError
        return null
    }
    return parsed
}
