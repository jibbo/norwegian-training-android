package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class MoveToNextPhaseDomainService @Inject constructor(
    val workoutRepository: WorkoutRepository,
) {

    private val workoutToPhasesConverter = WorkoutToPhasesConverter

    suspend operator fun invoke(id: Long, step: Int): Phase {
        val workout = workoutRepository.getById(id)
        if (workout == null && FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled) {
            FirebaseCrashlytics.getInstance()
                .log("[MoveToNextPhaseDomainService] Workout not found: $id")
        }
        val phases = workoutToPhasesConverter.convert(workout!!)
        val nextStep = (step + 1) % phases.size
        return phases[nextStep]
    }
}

object WorkoutToPhasesConverter {
    fun convert(workout: Workout): List<Phase> {
        val phases = workout.getSplit()
        val list = mutableListOf<Phase>()
        list.add(Phase(PhaseName.GET_READY, 0L))
        list.add(Phase(PhaseName.WARMUP, phases[0]))
        for (i in 1..phases.size - 2) {
            val name = if (i % 2 == 0) {
                PhaseName.SOFT_PHASE
            } else {
                PhaseName.HARD_PHASE
            }
            list.add(Phase(name, phases[i]))
        }
        list.add(Phase(PhaseName.REST_PHASE, phases[phases.size - 1]))
        list.add(Phase(PhaseName.COMPLETED, 0L))
        return list
    }
}
