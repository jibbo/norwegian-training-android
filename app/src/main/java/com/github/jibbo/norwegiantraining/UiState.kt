package com.github.jibbo.norwegiantraining

data class UiState(
    val step: Int = -1,
    val isTimerRunning: Boolean = false,
    val targetTimeMillis: Long = 0L,
    val remainingTimeOnPauseMillis: Long = 0L
) {

    fun stepMessage() = message(step)
    fun nextMessage() = message(step + 1)

    fun description() = when {
        step < 0 -> R.string.get_ready_desc
        step == 0 -> R.string.warmup_desc
        step == 9 -> R.string.cooldown_desc
        step > 9 -> R.string.completed_desc
        step % 2 == 1 -> R.string.hit_cardio_desc
        step % 2 == 0 -> R.string.light_cardio_desc
        else -> throw IllegalStateException("Steps out of bound")
    }

    private fun message(step: Int) = when {
        step < 0 -> R.string.get_ready
        step == 0 -> R.string.warmup
        step == 9 -> R.string.cooldown
        step == 10 -> R.string.completed
        step > 10 -> R.string.warmup
        step % 2 == 1 -> R.string.hit_cardio
        step % 2 == 0 -> R.string.light_cardio
        else -> throw IllegalStateException("Steps out of bound")
    }
}

enum class SpeakState(val message: Int) {
    ONE_MINUTE_REMAINING(R.string.one_minute_remaining),
    THREE(R.string.three),
    TWO(R.string.two),
    ONE(R.string.one),
    NOTHING(R.string.empty);

    companion object {
        fun from(timeRemaining: Int): SpeakState = when (timeRemaining) {
            -60 -> SpeakState.ONE_MINUTE_REMAINING
            -3 -> SpeakState.THREE
            -2 -> SpeakState.TWO
            -1 -> SpeakState.ONE
            else -> SpeakState.NOTHING
        }
    }
}
