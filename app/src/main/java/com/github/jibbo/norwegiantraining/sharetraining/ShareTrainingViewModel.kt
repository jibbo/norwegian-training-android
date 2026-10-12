package com.github.jibbo.norwegiantraining.sharetraining

import androidx.lifecycle.ViewModel
import com.github.jibbo.norwegiantraining.components.removeEmoji
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

data class ShareTrainingUiState(
    val workoutName: String = "",
    val completedAtMillis: Long = 0L,
    val totalPhases: Int = 0,
    val workoutDurationMinutes: Int = 0,
    val backgroundUri: String? = null,
) {
    val canShare: Boolean = backgroundUri != null
    val formattedDate: String
        get() = if (completedAtMillis <= 0L) {
            ""
        } else {
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(completedAtMillis))
        }
}

@HiltViewModel
class ShareTrainingViewModel @Inject constructor() : ViewModel() {
    private val states = MutableStateFlow(ShareTrainingUiState())
    val uiStates = states.asStateFlow()

    fun initialize(
        workoutName: String?,
        completedAtMillis: Long,
        totalPhases: Int,
        workoutDurationMinutes: Int,
    ) {
        if (states.value.workoutName.isNotBlank()) return
        states.value = states.value.copy(
            workoutName = workoutName.orEmpty().removeEmoji().replaceFirst(" ", ""),
            completedAtMillis = completedAtMillis.takeIf { it > 0L } ?: System.currentTimeMillis(),
            totalPhases = totalPhases.coerceAtLeast(0),
            workoutDurationMinutes = workoutDurationMinutes.coerceAtLeast(0),
        )
    }

    fun onBackgroundSelected(uri: String?) {
        if (uri.isNullOrBlank()) return
        states.value = states.value.copy(backgroundUri = uri)
    }
}
