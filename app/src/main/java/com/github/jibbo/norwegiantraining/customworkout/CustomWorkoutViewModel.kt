package com.github.jibbo.norwegiantraining.customworkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutDraft
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutField
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutValidationError
import com.github.jibbo.norwegiantraining.domain.calculateCustomWorkoutDifficulty
import com.github.jibbo.norwegiantraining.domain.generateCustomWorkoutContent
import com.github.jibbo.norwegiantraining.domain.parseCustomWorkoutContent
import com.github.jibbo.norwegiantraining.domain.validateCustomWorkoutDraft
import com.github.jibbo.norwegiantraining.service.WorkoutTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CustomWorkoutFormMode {
    CREATE,
    EDIT,
}

enum class CustomWorkoutPersistenceError {
    DATABASE_FAILURE,
    NOT_FOUND,
    MALFORMED_CONTENT,
    ACTIVE_WORKOUT,
}

data class CustomWorkoutFormState(
    val workoutId: Long? = null,
    val mode: CustomWorkoutFormMode = CustomWorkoutFormMode.CREATE,
    val isCustom: Boolean = true,
    val draft: CustomWorkoutDraft = CustomWorkoutDraft(),
    val loadedDraft: CustomWorkoutDraft? = null,
    val loadedContent: String? = null,
    val loadedDifficulty: Difficulty? = null,
    val validationErrors: Map<CustomWorkoutField, CustomWorkoutValidationError> = emptyMap(),
    val persistenceError: CustomWorkoutPersistenceError? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val deleteRequested: Boolean = false,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
    val notFound: Boolean = false,
)

@HiltViewModel
class CustomWorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val timerStateManager: WorkoutTimerManager
) : ViewModel() {

    private val states = MutableStateFlow(CustomWorkoutFormState())
    val uiState = states.asStateFlow()

    val timerState = timerStateManager.getWorkoutTimerState()

    fun initialize(workoutId: Long?): Job? {
        val mode = if (workoutId == null) {
            CustomWorkoutFormMode.CREATE
        } else {
            CustomWorkoutFormMode.EDIT
        }
        states.value = CustomWorkoutFormState(
            workoutId = workoutId,
            mode = mode,
            isLoading = workoutId != null,
        )

        return if (workoutId != null) {
            viewModelScope.launch(Dispatchers.Unconfined) {
                val workout = try {
                    workoutRepository.getById(workoutId)
                } catch (_: Throwable) {
                    states.value = states.value.copy(
                        isLoading = false,
                        persistenceError = CustomWorkoutPersistenceError.DATABASE_FAILURE,
                    )
                    return@launch
                }
                if (workout == null) {
                    states.value = states.value.copy(
                        isLoading = false,
                        notFound = true,
                        persistenceError = CustomWorkoutPersistenceError.NOT_FOUND,
                    )
                } else {
                    val parsedContent = parseCustomWorkoutContent(workout.content)
                    val draft = parsedContent?.let {
                        states.value.draft.copy(
                            name = workout.name,
                            icon = workout.icon,
                            workMinutes = it.workMinutes,
                            workSeconds = it.workSeconds,
                            restMinutes = it.restMinutes,
                            restSeconds = it.restSeconds,
                            rounds = it.rounds,
                        )
                    }
                    states.value = states.value.copy(
                        isLoading = false,
                        isCustom = workout.isCustom,
                        draft = draft ?: states.value.draft.copy(name = workout.name, icon = workout.icon),
                        loadedDraft = draft,
                        loadedContent = workout.content,
                        loadedDifficulty = workout.difficulty,
                        persistenceError = if (draft == null) {
                            CustomWorkoutPersistenceError.MALFORMED_CONTENT
                        } else {
                            null
                        },
                    )
                }
            }
        } else {
            null
        }
    }

    fun updateDraft(draft: CustomWorkoutDraft) {
        states.value = states.value.copy(
            draft = draft,
            validationErrors = emptyMap(),
            persistenceError = null,
        )
    }

    fun updateName(value: String) = updateDraft(states.value.draft.copy(name = value))
    fun updateIcon(value: String?) = updateDraft(states.value.draft.copy(icon = value))
    fun updateWorkMinutes(value: String) = updateDraft(states.value.draft.copy(workMinutes = value))
    fun updateWorkSeconds(value: String) = updateDraft(states.value.draft.copy(workSeconds = value))
    fun updateRestMinutes(value: String) = updateDraft(states.value.draft.copy(restMinutes = value))
    fun updateRestSeconds(value: String) = updateDraft(states.value.draft.copy(restSeconds = value))
    fun updateRounds(value: String) = updateDraft(states.value.draft.copy(rounds = value))

    fun validate(): Boolean {
        val result = validateCustomWorkoutDraft(states.value.draft)
        states.value = states.value.copy(validationErrors = result.errors)
        return result.isValid
    }

    fun requestDelete() {
        if (states.value.mode == CustomWorkoutFormMode.EDIT &&
            states.value.isCustom &&
            states.value.workoutId != null
        ) {
            states.value = states.value.copy(deleteRequested = true)
        }
    }

    fun clearDeleteRequest() {
        states.value = states.value.copy(deleteRequested = false)
    }

    fun delete(isWorkoutActive: Boolean = false): Job? {
        if (isWorkoutActive) {
            states.value = states.value.copy(
                deleteRequested = false,
                persistenceError = CustomWorkoutPersistenceError.ACTIVE_WORKOUT,
            )
            return null
        }
        val currentState = states.value
        val workoutId = currentState.workoutId
        if (currentState.mode != CustomWorkoutFormMode.EDIT ||
            workoutId == null ||
            currentState.isDeleting
        ) return null

        states.value = currentState.copy(
            deleteRequested = false,
            isDeleting = true,
            persistenceError = null,
        )
        return viewModelScope.launch {
            runCatching { workoutRepository.deleteCustom(workoutId) }
                .onSuccess { deleted ->
                    if (deleted) {
                        states.value = states.value.copy(isDeleting = false, deleted = true)
                    } else {
                        states.value = states.value.copy(
                            isDeleting = false,
                            notFound = true,
                            persistenceError = CustomWorkoutPersistenceError.NOT_FOUND,
                        )
                    }
                }
                .onFailure {
                    states.value = states.value.copy(
                        isDeleting = false,
                        persistenceError = CustomWorkoutPersistenceError.DATABASE_FAILURE,
                    )
                }
        }
    }

    fun clearPersistenceError() {
        states.value = states.value.copy(persistenceError = null)
    }

    fun save(
        isWorkoutActive: Boolean = false,
        fallbackName: String = "Custom",
    ): Job? {
        if (isWorkoutActive) {
            states.value = states.value.copy(
                persistenceError = CustomWorkoutPersistenceError.ACTIVE_WORKOUT,
            )
            return null
        }
        if (states.value.isSaving) return null
        if (states.value.mode == CustomWorkoutFormMode.EDIT &&
            states.value.persistenceError == CustomWorkoutPersistenceError.MALFORMED_CONTENT
        ) return null

        val validation = validateCustomWorkoutDraft(states.value.draft)
        states.value = states.value.copy(validationErrors = validation.errors)
        if (!validation.isValid) return null

        val currentState = states.value
        val unchangedEdit = currentState.mode == CustomWorkoutFormMode.EDIT &&
            currentState.draft == currentState.loadedDraft
        val content = if (unchangedEdit) currentState.loadedContent else generateCustomWorkoutContent(validation)
        val difficulty = if (unchangedEdit || !currentState.isCustom) {
            currentState.loadedDifficulty
        } else {
            calculateCustomWorkoutDifficulty(validation)
        }
        if (content == null || difficulty == null) {
            states.value = states.value.copy(
                persistenceError = CustomWorkoutPersistenceError.DATABASE_FAILURE,
            )
            return null
        }

        states.value = currentState.copy(isSaving = true, persistenceError = null)
        return viewModelScope.launch {
            runCatching {
                val workout = Workout(
                    id = currentState.workoutId ?: 0L,
                    name = validation.trimmedName.ifBlank { fallbackName },
                    difficulty = difficulty,
                    content = content,
                    isCustom = if (currentState.mode == CustomWorkoutFormMode.CREATE) true else currentState.isCustom,
                    icon = validation.icon,
                )
                if (currentState.mode == CustomWorkoutFormMode.CREATE) {
                    workoutRepository.insertCustom(workout)
                    true
                } else {
                    workoutRepository.updateById(workout)
                }
            }.onSuccess { updated ->
                if (updated) {
                    states.value = states.value.copy(isSaving = false, saved = true)
                } else {
                    states.value = states.value.copy(
                        isSaving = false,
                        persistenceError = CustomWorkoutPersistenceError.NOT_FOUND,
                        notFound = true,
                    )
                }
            }.onFailure {
                states.value = states.value.copy(
                    isSaving = false,
                    persistenceError = CustomWorkoutPersistenceError.DATABASE_FAILURE,
                )
            }
        }
    }
}
