@file:SuppressLint("NewApi")

package com.github.jibbo.norwegiantraining.domain

import android.annotation.SuppressLint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ManualWorkoutUiState(
    val sheetVisible: Boolean = false,
    val draft: ManualWorkoutDraft = ManualWorkoutDraft(),
    val fieldErrors: Map<ManualWorkoutField, ManualWorkoutValidationError> = emptyMap(),
    val isSaving: Boolean = false,
    val persistenceError: Boolean = false,
    val saveCompleted: Boolean = false,
)

@HiltViewModel
class ManualWorkoutViewModel @Inject constructor(
    private val saveManualWorkout: SaveManualWorkoutUseCase,
) : ViewModel() {
    private val states = MutableStateFlow(ManualWorkoutUiState())
    val uiState = states.asStateFlow()

    fun open(today: LocalDate = LocalDate.now()) {
        states.value = ManualWorkoutUiState(
            sheetVisible = true,
            draft = ManualWorkoutDraft(date = today),
        )
    }

    fun dismiss() {
        states.value = states.value.copy(
            sheetVisible = false,
            isSaving = false,
            persistenceError = false,
            fieldErrors = emptyMap(),
            saveCompleted = false,
        )
    }

    fun selectType(type: ManualWorkoutType?) = updateDraft { copy(type = type) }

    fun updateDate(date: LocalDate) = updateDraft { copy(date = date) }

    fun updateHours(hours: String) = updateDraft { copy(hours = hours) }

    fun updateMinutes(minutes: String) = updateDraft { copy(minutes = minutes) }

    fun clearPersistenceError() {
        states.value = states.value.copy(persistenceError = false)
    }

    fun consumeSaveCompleted() {
        states.value = states.value.copy(saveCompleted = false)
    }

    fun submit(translatedName: String) {
        val current = states.value
        if (!current.sheetVisible || current.isSaving) return

        val validation = validateManualWorkoutDraft(current.draft)
        if (!validation.isValid) {
            states.value = current.copy(fieldErrors = validation.errors)
            return
        }

        states.value = current.copy(
            isSaving = true,
            fieldErrors = emptyMap(),
            persistenceError = false,
        )
        viewModelScope.launch {
            when (val result = saveManualWorkout(validation, translatedName)) {
                is ManualWorkoutSaveResult.Success -> {
                    states.value = states.value.copy(
                        sheetVisible = false,
                        isSaving = false,
                        persistenceError = false,
                        saveCompleted = true,
                    )
                }
                ManualWorkoutSaveResult.InvalidInput -> {
                    states.value = states.value.copy(
                        isSaving = false,
                        fieldErrors = validation.errors,
                    )
                }
                is ManualWorkoutSaveResult.DatabaseFailure -> {
                    states.value = states.value.copy(
                        isSaving = false,
                        persistenceError = true,
                    )
                }
            }
        }
    }

    private fun updateDraft(update: ManualWorkoutDraft.() -> ManualWorkoutDraft) {
        states.value = states.value.copy(
            draft = states.value.draft.update(),
            fieldErrors = emptyMap(),
            persistenceError = false,
            saveCompleted = false,
        )
    }
}
