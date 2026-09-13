package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.testutils.FakeWorkoutRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutTimerBehaviorTest {
    @Test
    fun `custom workout with eight pairs keeps every interval and final rest`() {
        val content = buildString {
            append("5m-")
            repeat(8) { append("30s-15s-") }
            append("5m")
        }

        val phases = WorkoutToPhasesConverter.convert(workout(content)).getOrThrow()

        assertEquals(20, phases.size)
        assertEquals(PhaseName.GET_READY, phases.first().name)
        assertEquals(PhaseName.WARMUP, phases[1].name)
        assertEquals(8, phases.count { it.name == PhaseName.HARD_PHASE })
        assertEquals(8, phases.count { it.name == PhaseName.SOFT_PHASE })
        assertEquals(PhaseName.REST_PHASE, phases[18].name)
        assertEquals(300_000L, phases[18].durationMillis)
        assertEquals(PhaseName.COMPLETED, phases.last().name)
    }

    @Test
    fun `phase advancement reports malformed custom content safely`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insertCustom(workout("5m-invalid-5m"))
        val service = MoveToNextPhaseDomainService(repository)

        val result = service(1L, 0)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MalformedWorkoutException)
    }

    @Test
    fun `custom timer workout retains shared display label`() {
        val workout = workout("5m-30s-15s-5m").copy(icon = "🔥", name = "Intervals")

        assertEquals("🔥 Intervals", workout.displayLabel())
    }

    private fun workout(content: String) = Workout(
        id = 1L,
        name = "Workout",
        difficulty = Difficulty.BEGINNER,
        content = content,
        isCustom = true,
    )
}
