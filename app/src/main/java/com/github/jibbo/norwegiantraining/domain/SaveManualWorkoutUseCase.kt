package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

sealed interface ManualWorkoutSaveResult {
    data class Success(val session: Session) : ManualWorkoutSaveResult
    data object InvalidInput : ManualWorkoutSaveResult
    data class DatabaseFailure(val cause: Throwable) : ManualWorkoutSaveResult
}

class SaveManualWorkoutUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val analytics: Analytics,
) {
    suspend operator fun invoke(
        validation: ManualWorkoutValidationResult,
        translatedName: String,
    ): ManualWorkoutSaveResult {
        val value = validation.value ?: return ManualWorkoutSaveResult.InvalidInput
        if (!validation.isValid || translatedName.isBlank()) {
            return ManualWorkoutSaveResult.InvalidInput
        }

        val session = Session(
            date = Date.from(value.date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            isManual = true,
            name = translatedName,
            duration = value.durationMinutes,
            phasesEnded = 1,
            skipCount = 0,
        )

        return try {
            val id = sessionRepository.insertManualSession(session)
            val saved = session.copy(id = id)
            analytics.logManualWorkoutLogged()
            ManualWorkoutSaveResult.Success(saved)
        } catch (exception: Exception) {
            ManualWorkoutSaveResult.DatabaseFailure(exception)
        }
    }
}
