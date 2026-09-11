package com.github.jibbo.norwegiantraining.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ManualWorkoutTest {
    private val today = LocalDate.of(2026, 9, 11)

    @Test
    fun `types remain in product order`() {
        assertEquals(
            listOf(
                ManualWorkoutType.RUN,
                ManualWorkoutType.STRENGTH_TRAINING,
                ManualWorkoutType.CYCLING,
                ManualWorkoutType.SWIMMING,
                ManualWorkoutType.WALKING,
                ManualWorkoutType.HIIT,
            ),
            ManualWorkoutType.entries,
        )
    }

    @Test
    fun `draft defaults to no type today and one hour`() {
        val draft = ManualWorkoutDraft(date = today)

        assertNull(draft.type)
        assertEquals(today, draft.date)
        assertEquals("01", draft.hours)
        assertEquals("00", draft.minutes)
    }

    @Test
    fun `validates minimum and maximum date boundaries`() {
        val firstDay = today.withDayOfYear(1)
        assertTrue(valid(ManualWorkoutDraft(ManualWorkoutType.RUN, firstDay), today))
        assertTrue(valid(ManualWorkoutDraft(ManualWorkoutType.RUN, today), today))
        assertFalse(valid(ManualWorkoutDraft(ManualWorkoutType.RUN, firstDay.minusDays(1)), today))
        assertFalse(valid(ManualWorkoutDraft(ManualWorkoutType.RUN, today.plusDays(1)), today))
    }

    @Test
    fun `accepts duration boundaries and converts to minutes`() {
        assertEquals(1L, validValue(hours = "00", minutes = "01").durationMinutes)
        assertEquals(1439L, validValue(hours = "23", minutes = "59").durationMinutes)
        assertEquals(1440L, validValue(hours = "24", minutes = "00").durationMinutes)
    }

    @Test
    fun `rejects zero and durations over twenty four hours`() {
        val zero = validate(ManualWorkoutDraft(ManualWorkoutType.RUN, today, "00", "00"))
        val tooLong = validate(ManualWorkoutDraft(ManualWorkoutType.RUN, today, "24", "01"))

        assertEquals(ManualWorkoutValidationError.DURATION_ZERO, zero.errors[ManualWorkoutField.MINUTES])
        assertEquals(ManualWorkoutValidationError.DURATION_TOO_LONG, tooLong.errors[ManualWorkoutField.HOURS])
    }

    @Test
    fun `rejects non two digit and out of range values`() {
        val invalid = validate(ManualWorkoutDraft(ManualWorkoutType.RUN, today, "1", "60"))

        assertEquals(ManualWorkoutValidationError.HOURS_FORMAT, invalid.errors[ManualWorkoutField.HOURS])
        assertEquals(ManualWorkoutValidationError.MINUTES_OUT_OF_RANGE, invalid.errors[ManualWorkoutField.MINUTES])
    }

    @Test
    fun `requires a workout type`() {
        val result = validate(ManualWorkoutDraft(date = today))

        assertEquals(ManualWorkoutValidationError.TYPE_REQUIRED, result.errors[ManualWorkoutField.TYPE])
        assertFalse(result.isValid)
    }

    private fun valid(
        draft: ManualWorkoutDraft,
        referenceDate: LocalDate,
    ): Boolean = validate(draft, referenceDate).isValid

    private fun validValue(
        hours: String,
        minutes: String,
    ): ValidatedManualWorkout = validate(
        ManualWorkoutDraft(ManualWorkoutType.RUN, today, hours, minutes),
    ).value.also { assertNotNull(it) }!!

    private fun validate(
        draft: ManualWorkoutDraft,
        referenceDate: LocalDate = today,
    ): ManualWorkoutValidationResult = validateManualWorkoutDraft(draft, referenceDate)
}
