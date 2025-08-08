package com.github.jibbo.norwegiantraining.domain

import javax.inject.Inject

class MoveToNextPhaseDomainService @Inject constructor(
    private val getTodaySessionUseCase: GetTodaySessionUseCase,
    private val saveTodaySession: SaveTodaySession,
) {
    suspend operator fun invoke(): Phase {
        val session = getTodaySessionUseCase()
        val nextStep = session.phasesEnded + 1
        saveTodaySession(session.copy(phasesEnded = nextStep))
        return Phase.fromNumber(nextStep)
    }
}

enum class Phase(val durationMillis: Long? = null) {
    GET_READY(),
    WARMUP(10 * 60 * 1000), // 10 minutes
    HARD_PHASE(4 * 60 * 1000), // 4 minutes
    SOFT_PHASE(4 * 60 * 1000), // 4 minutes
    REST_PHASE(5 * 60 * 1000), // 5 minutes
    COMPLETED();

    companion object {
        fun fromNumber(number: Int) = when {
            number == 0 -> GET_READY
            number == 1 -> WARMUP
            number == 9 -> REST_PHASE
            number == 10 -> COMPLETED
            number % 2 == 1 -> HARD_PHASE
            number % 2 == 0 -> SOFT_PHASE
            else -> throw IllegalArgumentException("Invalid number: $number")
        }
    }
}
