package com.github.jibbo.norwegiantraining.customworkout

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CustomWorkoutFormMode {
    CREATE,
    EDIT,
}

data class CustomWorkoutFormState(
    val workoutId: Long? = null,
    val mode: CustomWorkoutFormMode = CustomWorkoutFormMode.CREATE,
)

class CustomWorkoutViewModel : ViewModel() {
    private val states = MutableStateFlow(CustomWorkoutFormState())
    val uiState = states.asStateFlow()

    fun initialize(workoutId: Long?) {
        states.value = CustomWorkoutFormState(
            workoutId = workoutId,
            mode = if (workoutId == null) {
                CustomWorkoutFormMode.CREATE
            } else {
                CustomWorkoutFormMode.EDIT
            },
        )
    }
}
