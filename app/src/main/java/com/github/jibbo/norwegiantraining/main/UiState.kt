package com.github.jibbo.norwegiantraining.main

import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.domain.Phase
import com.github.jibbo.norwegiantraining.domain.PhaseName

data class UiState(
    val step: Phase = Phase(PhaseName.GET_READY, 0L),
    val isTimerRunning: Boolean = false,
    val targetTimeMillis: Long = 0L,
    val remainingTimeOnPauseMillis: Long = 0L,
    val workoutName: String = "",
)

fun PhaseName.description() = when (this) {
    PhaseName.GET_READY -> R.string.get_ready_desc
    PhaseName.WARMUP -> R.string.warmup_desc
    PhaseName.REST_PHASE -> R.string.cooldown_desc
    PhaseName.COMPLETED -> R.string.completed_desc
    PhaseName.HARD_PHASE -> R.string.hit_cardio_desc
    PhaseName.SOFT_PHASE -> R.string.light_cardio_desc
}

fun PhaseName.message() = when (this) {
    PhaseName.GET_READY -> R.string.get_ready
    PhaseName.WARMUP -> R.string.warmup
    PhaseName.REST_PHASE -> R.string.cooldown
    PhaseName.COMPLETED -> R.string.completed
    PhaseName.HARD_PHASE -> R.string.hit_cardio
    PhaseName.SOFT_PHASE -> R.string.light_cardio
}

sealed class SpeakState(val message: Int) {
    object OneMinute : SpeakState(R.string.one_minute_remaining)
    object ThreeSeconds : SpeakState(R.string.three)
    object TwoSeconds : SpeakState(R.string.two)
    object OneSecond : SpeakState(R.string.one)
    class Message(message: Int) : SpeakState(message)
    object Nothing : SpeakState(R.string.empty)

    companion object {
        fun fromSeconds(timeRemaining: Int): SpeakState = when (timeRemaining) {
            -60 -> OneMinute
            -3 -> ThreeSeconds
            -2 -> TwoSeconds
            -1 -> OneSecond
            else -> Nothing
        }
    }
}
