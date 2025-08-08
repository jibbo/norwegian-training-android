package com.github.jibbo.norwegiantraining.main

import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.domain.Phase

data class UiState(
    val step: Phase = Phase.GET_READY,
    val isTimerRunning: Boolean = false,
    val targetTimeMillis: Long = 0L,
    val remainingTimeOnPauseMillis: Long = 0L,
    val name: String = "",
)

fun Phase.description() = when (this) {
    Phase.GET_READY -> R.string.get_ready_desc
    Phase.WARMUP -> R.string.warmup_desc
    Phase.REST_PHASE -> R.string.cooldown_desc
    Phase.COMPLETED -> R.string.completed_desc
    Phase.HARD_PHASE -> R.string.hit_cardio_desc
    Phase.SOFT_PHASE -> R.string.light_cardio_desc
}

fun Phase.message() = when (this) {
    Phase.GET_READY -> R.string.get_ready
    Phase.WARMUP -> R.string.warmup
    Phase.REST_PHASE -> R.string.cooldown
    Phase.COMPLETED -> R.string.completed
    Phase.HARD_PHASE -> R.string.hit_cardio
    Phase.SOFT_PHASE -> R.string.light_cardio
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
