package com.github.jibbo.norwegiantraining.home

import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal object WorkoutTransitionState {
    const val TRANSITION_DURATION_MS = 250L

    private val _overlay = MutableStateFlow<OverlayState?>(null)
    val overlay: StateFlow<OverlayState?> = _overlay

    fun beginLaunch(workoutId: Long, bounds: Rect?) {
        _overlay.value = OverlayState(workoutId, bounds)
    }

    fun beginReturn(workoutId: Long, bounds: Rect?) {
        _overlay.value = OverlayState(workoutId, bounds)
    }

    fun clear() {
        _overlay.value = null
    }

    data class OverlayState(
        val workoutId: Long,
        val bounds: Rect?,
    )
}
