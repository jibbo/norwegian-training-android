package com.github.jibbo.norwegiantraining.domain

import javax.inject.Inject

class MoveToNextPhaseDomainService @Inject constructor(
    private val getTodaySessionUseCase: GetTodaySessionUseCase
) {
//    suspend operator fun invoke(): Phase {
//        val session = getTodaySessionUseCase()
//        val nextStep = (session.phasesEnded + session.skipCount) + 1
//        return Phase.fromNumber(nextStep)
//    }

    operator fun invoke(step: Int): Phase {
        val nextStep = step + 1
        return Phase.fromNumber(nextStep)
    }
}
