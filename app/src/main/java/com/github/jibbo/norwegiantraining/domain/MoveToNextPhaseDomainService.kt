package com.github.jibbo.norwegiantraining.domain

import javax.inject.Inject

class MoveToNextPhaseDomainService @Inject constructor(
    private val getTodaySessionUseCase: GetTodaySessionUseCase
) {
    suspend operator fun invoke(): Phase {
        val session = getTodaySessionUseCase()
        val nextStep = (session.phasesEnded + session.skipCount) + 1
        return Phase.fromNumber(nextStep % 12)
    }
}
