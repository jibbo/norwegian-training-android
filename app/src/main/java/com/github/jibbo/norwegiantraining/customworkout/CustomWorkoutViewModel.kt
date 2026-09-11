package com.github.jibbo.norwegiantraining.customworkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutDraft
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutField
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutValidationError
import com.github.jibbo.norwegiantraining.domain.validateCustomWorkoutDraft
import dagger.hilt.android.lifecycle.HiltViewModel
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
    ACTIVE_WORKOUT,
}

data class CustomWorkoutFormState(
    val workoutId: Long? = null,
    val mode: CustomWorkoutFormMode = CustomWorkoutFormMode.CREATE,
    val draft: CustomWorkoutDraft = CustomWorkoutDraft(),
    val validationErrors: Map<CustomWorkoutField, CustomWorkoutValidationError> = emptyMap(),
    val persistenceError: CustomWorkoutPersistenceError? = null,
    val isLoading: Boolean = false,
    val notFound: Boolean = false,
)

@HiltViewModel
class CustomWorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val states = MutableStateFlow(CustomWorkoutFormState())
    val uiState = states.asStateFlow()

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
            viewModelScope.launch {
                val workout = runCatching { workoutRepository.getById(workoutId) }.getOrNull()
                if (workout == null) {
                    states.value = states.value.copy(
                        isLoading = false,
                        notFound = true,
                        persistenceError = CustomWorkoutPersistenceError.NOT_FOUND,
                    )
                } else {
                    states.value = states.value.copy(
                        isLoading = false,
                        draft = states.value.draft.copy(
                            name = workout.name,
                            icon = workout.icon,
                        ),
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
}
