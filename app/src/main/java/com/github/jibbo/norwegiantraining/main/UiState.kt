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

enum class SpeakState(val message: Int) {
    ONE_MINUTE_REMAINING(R.string.one_minute_remaining),
    THREE(R.string.three),
    TWO(R.string.two),
    ONE(R.string.one),
    NOTHING(R.string.empty);

    companion object {
        fun from(timeRemaining: Int): SpeakState = when (timeRemaining) {
            -60 -> ONE_MINUTE_REMAINING
            -3 -> THREE
            -2 -> TWO
            -1 -> ONE
            else -> NOTHING
        }
    }
}
