package com.github.jibbo.norwegiantraining.customworkout

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomWorkoutIconPickerTest {
    @Test
    fun `new picker uses shoe default`() {
        assertEquals(DEFAULT_CUSTOM_WORKOUT_ICON, CustomWorkoutIconPickerState().selectedIcon)
    }

    @Test
    fun `selection replaces default`() {
        assertEquals("🔥", CustomWorkoutIconPickerState().select("🔥").selectedIcon)
    }

    @Test
    fun `blank selection is cleared`() {
        assertEquals(null, CustomWorkoutIconPickerState("🔥").select(" ").selectedIcon)
    }

    @Test
    fun `clear keeps edit state empty`() {
        val cleared = CustomWorkoutIconPickerState("🔥").clear()

        assertEquals(null, cleared.selectedIcon)
        assertEquals(null, cleared.select(null).selectedIcon)
    }
}
