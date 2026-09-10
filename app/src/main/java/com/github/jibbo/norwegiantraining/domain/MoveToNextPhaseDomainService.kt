package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class WorkoutNotFoundException(id: Long) :
    IllegalArgumentException("Workout not found: $id")

class MalformedWorkoutException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)

class MoveToNextPhaseDomainService @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(id: Long, step: Int): Result<Phase> {
        val workout = workoutRepository.getById(id)
        if (workout == null) {
            if (FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled) {
                FirebaseCrashlytics.getInstance()
                    .log("[MoveToNextPhaseDomainService] Workout not found: $id")
            }
            return Result.failure(WorkoutNotFoundException(id))
        }

        val phases = WorkoutToPhasesConverter.convert(workout).getOrElse {
            return Result.failure(it)
        }
        val nextStep = (step + 1) % phases.size
        return Result.success(phases[nextStep])
    }
}

object WorkoutToPhasesConverter {
    const val GET_READY_COUNTDOWN_DURATION = 10_000L

    fun convert(workout: Workout): Result<List<Phase>> = runCatching {
        val phases = try {
            workout.getSplit()
        } catch (error: RuntimeException) {
            throw MalformedWorkoutException(
                "Malformed workout content for ${workout.id}",
                error,
            )
        }

        if (phases.size < 2 || phases.any { it <= 0L }) {
            throw MalformedWorkoutException("Workout ${workout.id} has invalid phase content")
        }

        buildList {
            add(Phase(PhaseName.GET_READY, GET_READY_COUNTDOWN_DURATION))
            add(Phase(PhaseName.WARMUP, phases.first()))
            for (i in 1 until phases.lastIndex) {
                val name = if (i % 2 == 0) {
                    PhaseName.SOFT_PHASE
                } else {
                    PhaseName.HARD_PHASE
                }
                add(Phase(name, phases[i]))
            }
            add(Phase(PhaseName.REST_PHASE, phases.last()))
            add(Phase(PhaseName.COMPLETED, 0L))
        }
    }
}
