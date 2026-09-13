package com.github.jibbo.norwegiantraining.testutils

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeWorkoutRepositoryTest {
    @Test
    fun `custom and built-in flows stay separated and ordered`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(
            workout(id = 10, name = "Built-in", isCustom = false),
            workout(id = 20, name = "Older", isCustom = true),
            workout(id = 30, name = "Newer", isCustom = true),
        )

        assertEquals(listOf(30L, 20L), repository.getCustomWorkouts().first().map { it.id })
        assertEquals(listOf(10L), repository.getBuiltInWorkouts().first().map { it.id })
    }

    @Test
    fun `custom insert assigns an ID and remains observable`() = runTest {
        val repository = FakeWorkoutRepository()

        val id = repository.insertCustom(workout(name = "Custom"))

        assertEquals(1L, id)
        assertEquals(true, repository.getById(id)?.isCustom)
        assertEquals(listOf(id), repository.getCustomWorkouts().first().map { it.id })
    }

    @Test
    fun `custom update preserves ID and missing update fails`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insertCustom(workout(name = "Before"))
        val existing = repository.getCustomWorkouts().first().single()

        assertTrue(repository.updateById(existing.copy(name = "After", icon = "🔥")))
        assertEquals("After", repository.getById(existing.id)?.name)
        assertEquals("🔥", repository.getById(existing.id)?.icon)
        assertEquals(existing.id, repository.getById(existing.id)?.id)
        assertFalse(repository.updateById(workout(id = 999, name = "Missing", isCustom = true)))
    }

    @Test
    fun `custom delete does not delete built-ins`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(
            workout(id = 1, name = "Built-in", isCustom = false),
            workout(id = 2, name = "Custom", isCustom = true),
        )

        assertTrue(repository.deleteCustom(2))
        assertFalse(repository.deleteCustom(2))
        assertEquals("Built-in", repository.getById(1)?.name)
        assertEquals(1, repository.getAll().first().size)
    }

    @Test
    fun `configured failure is propagated without mutating state`() = runTest {
        val repository = FakeWorkoutRepository()
        val failure = IllegalStateException("database failure")
        repository.failure = failure

        try {
            repository.insertCustom(workout(name = "Rejected"))
            throw AssertionError("Expected failure")
        } catch (actual: IllegalStateException) {
            assertEquals(failure.message, actual.message)
        }
        assertTrue(repository.getAll().first().isEmpty())
    }

    private fun workout(
        id: Long = 0,
        name: String,
        isCustom: Boolean = true,
    ) = Workout(
        id = id,
        name = name,
        difficulty = Difficulty.BEGINNER,
        content = "5m-1m-1m-5m",
        isCustom = isCustom,
    )
}
