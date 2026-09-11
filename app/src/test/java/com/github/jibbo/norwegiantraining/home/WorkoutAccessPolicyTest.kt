package com.github.jibbo.norwegiantraining.home

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutAccessPolicyTest {
    @Test
    fun `custom workouts launch without trial or entitlement`() {
        assertTrue(canLaunchWorkout(workout(isCustom = true), isFreeTrial = false, hasEntitlement = false))
    }

    @Test
    fun `built-in workouts require trial or entitlement`() {
        val builtIn = workout(isCustom = false)

        assertFalse(canLaunchWorkout(builtIn, isFreeTrial = false, hasEntitlement = false))
        assertTrue(canLaunchWorkout(builtIn, isFreeTrial = true, hasEntitlement = false))
        assertTrue(canLaunchWorkout(builtIn, isFreeTrial = false, hasEntitlement = true))
    }

    private fun workout(isCustom: Boolean) = Workout(
        name = "Workout",
        difficulty = Difficulty.BEGINNER,
        content = "5m-30s-15s-5m",
        isCustom = isCustom,
    )
}
