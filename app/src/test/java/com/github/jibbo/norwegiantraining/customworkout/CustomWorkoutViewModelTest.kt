package com.github.jibbo.norwegiantraining.customworkout

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutDraft
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutField
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutValidationError
import com.github.jibbo.norwegiantraining.testutils.FakeWorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomWorkoutViewModelTest {
    @Test
    fun `null id selects create mode`() {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepository())

        viewModel.initialize(null)

        assertEquals(CustomWorkoutFormMode.CREATE, viewModel.uiState.value.mode)
        assertEquals(null, viewModel.uiState.value.workoutId)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `workout id selects edit mode and preserves id`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L))
        assertEquals("Existing", repository.getById(42L)?.name)
        val viewModel = CustomWorkoutViewModel(repository)

        viewModel.initialize(42L)?.join()

        assertEquals(CustomWorkoutFormMode.EDIT, viewModel.uiState.value.mode)
        assertEquals(42L, viewModel.uiState.value.workoutId)
        assertEquals("Existing", viewModel.uiState.value.draft.name)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `create form starts with default icon`() {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepository())

        viewModel.initialize(null)

        assertEquals(DEFAULT_CUSTOM_WORKOUT_ICON, viewModel.uiState.value.draft.icon)
    }

    @Test
    fun `cleared icon persists as null`() = runTest {
        val repository = FakeWorkoutRepository()
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(null)
        viewModel.updateIcon(null)
        viewModel.updateName("Plain")

        viewModel.save()?.join()

        assertEquals(null, repository.getCustomWorkouts().first().single().icon)
    }

    @Test
    fun `edit loading does not restore default for null icon`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L).copy(icon = null))
        val viewModel = CustomWorkoutViewModel(repository)

        viewModel.initialize(42L)?.join()

        assertEquals(null, viewModel.uiState.value.draft.icon)
    }

    @Test
    fun `field updates retain invalid text and clear stale errors`() {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepository())
        viewModel.updateRounds("not a number")

        assertEquals("not a number", viewModel.uiState.value.draft.rounds)
        assertFalse(viewModel.validate())
        assertEquals(
            CustomWorkoutValidationError.INVALID_NUMBER,
            viewModel.uiState.value.validationErrors[CustomWorkoutField.ROUNDS],
        )

        viewModel.updateRounds("8")

        assertEquals("8", viewModel.uiState.value.draft.rounds)
        assertTrue(viewModel.uiState.value.validationErrors.isEmpty())
    }

    @Test
    fun `missing edit id is represented as not found`() = runTest {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepository())

        viewModel.initialize(99L)?.join()

        assertTrue(viewModel.uiState.value.notFound)
        assertEquals(CustomWorkoutPersistenceError.NOT_FOUND, viewModel.uiState.value.persistenceError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `valid create saves generated custom workout and returns success`() = runTest {
        val repository = FakeWorkoutRepository()
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(null)
        viewModel.updateDraft(
            CustomWorkoutDraft(
                name = "Morning",
                icon = "🔥",
                workMinutes = "1",
                restMinutes = "0",
                restSeconds = "30",
                rounds = "2",
            )
        )

        viewModel.save()?.join()

        val stored = repository.getCustomWorkouts().first().single()
        assertTrue(viewModel.uiState.value.saved)
        assertEquals("Morning", stored.name)
        assertEquals("🔥", stored.icon)
        assertEquals("5m-1m-30s-1m-30s-5m", stored.content)
        assertTrue(stored.isCustom)
    }

    @Test
    fun `edit saves regenerated workout with stable id and cleared icon`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L))
        val viewModel = CustomWorkoutViewModel(repository)

        viewModel.initialize(42L)?.join()
        viewModel.updateDraft(
            CustomWorkoutDraft(
                name = "Updated",
                icon = null,
                workMinutes = "1",
                workSeconds = "0",
                restMinutes = "0",
                restSeconds = "30",
                rounds = "10",
            ),
        )
        viewModel.save()?.join()

        val updated = repository.getById(42L)
        assertTrue(viewModel.uiState.value.saved)
        assertEquals(42L, updated?.id)
        assertEquals("Updated", updated?.name)
        assertEquals(null, updated?.icon)
        assertEquals("5m-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-1m-30s-5m", updated?.content)
        assertEquals(Difficulty.INTERMEDIATE, updated?.difficulty)
    }
    @Test
    fun `invalid create retains errors and does not insert`() = runTest {
        val repository = FakeWorkoutRepository()
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(null)
        viewModel.updateRounds("0")

        assertEquals(null, viewModel.save())
        assertFalse(viewModel.uiState.value.saved)
        assertEquals(0, repository.getCustomWorkouts().first().size)
        assertEquals(CustomWorkoutValidationError.OUT_OF_RANGE, viewModel.uiState.value.validationErrors[CustomWorkoutField.ROUNDS])
    }

    @Test
    fun `create database failure is exposed without reporting success`() = runTest {
        val repository = FakeWorkoutRepository().apply { failure = IllegalStateException("db") }
        val viewModel = CustomWorkoutViewModel(repository)

        viewModel.save()?.join()

        assertFalse(viewModel.uiState.value.saved)
        assertEquals(CustomWorkoutPersistenceError.DATABASE_FAILURE, viewModel.uiState.value.persistenceError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `edit database failure is exposed without reporting success`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L))
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(42L)?.join()
        repository.failure = IllegalStateException("db")

        viewModel.save()?.join()

        assertFalse(viewModel.uiState.value.saved)
        assertEquals(CustomWorkoutPersistenceError.DATABASE_FAILURE, viewModel.uiState.value.persistenceError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `edit load database failure is not reported as missing`() = runTest {
        val repository = FakeWorkoutRepository().apply { failure = IllegalStateException("db") }
        val viewModel = CustomWorkoutViewModel(repository)

        viewModel.initialize(42L)?.join()

        assertFalse(viewModel.uiState.value.notFound)
        assertEquals(CustomWorkoutPersistenceError.DATABASE_FAILURE, viewModel.uiState.value.persistenceError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `delete request is available only in edit mode`() = runTest {
        val createViewModel = CustomWorkoutViewModel(FakeWorkoutRepository())
        createViewModel.initialize(null)
        createViewModel.requestDelete()
        assertFalse(createViewModel.uiState.value.deleteRequested)

        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L))
        val editViewModel = CustomWorkoutViewModel(repository)
        editViewModel.initialize(42L)?.join()
        editViewModel.requestDelete()

        assertTrue(editViewModel.uiState.value.deleteRequested)
        editViewModel.clearDeleteRequest()
        assertFalse(editViewModel.uiState.value.deleteRequested)
    }

    @Test
    fun `confirmed delete removes custom workout and reports deleted`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L))
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(42L)?.join()
        viewModel.requestDelete()

        viewModel.delete()?.join()

        assertTrue(viewModel.uiState.value.deleted)
        assertEquals(null, repository.getById(42L))
    }

    @Test
    fun `clearing delete request does not mutate workout`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L))
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(42L)?.join()
        viewModel.requestDelete()
        viewModel.clearDeleteRequest()

        assertFalse(viewModel.uiState.value.deleted)
        assertEquals("Existing", repository.getById(42L)?.name)
    }

    @Test
    fun `delete database failure is exposed without reporting deletion`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L))
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(42L)?.join()
        repository.failure = IllegalStateException("db")

        viewModel.requestDelete()
        viewModel.delete()?.join()

        assertFalse(viewModel.uiState.value.deleted)
        assertEquals(CustomWorkoutPersistenceError.DATABASE_FAILURE, viewModel.uiState.value.persistenceError)
        assertFalse(viewModel.uiState.value.isDeleting)
    }

    @Test
    fun `active workout blocks save without inserting`() = runTest {
        val repository = FakeWorkoutRepository()
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(null)
        viewModel.updateName("Blocked")

        assertEquals(null, viewModel.save(isWorkoutActive = true))
        assertEquals(CustomWorkoutPersistenceError.ACTIVE_WORKOUT, viewModel.uiState.value.persistenceError)
        assertEquals(0, repository.getCustomWorkouts().first().size)
    }

    @Test
    fun `active workout blocks delete without deleting`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L))
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(42L)?.join()
        viewModel.requestDelete()

        assertEquals(null, viewModel.delete(isWorkoutActive = true))
        assertEquals(CustomWorkoutPersistenceError.ACTIVE_WORKOUT, viewModel.uiState.value.persistenceError)
        assertEquals("Existing", repository.getById(42L)?.name)
    }

    private fun workout(id: Long) = Workout(
        id = id,
        name = "Existing",
        difficulty = Difficulty.BEGINNER,
        content = "5m-30s-15s-5m",
        isCustom = true,
    )
}
