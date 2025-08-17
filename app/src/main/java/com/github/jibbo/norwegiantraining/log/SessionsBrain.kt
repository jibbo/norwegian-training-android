package com.github.jibbo.norwegiantraining.log

import androidx.compose.ui.graphics.Color
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.ui.theme.Orange
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Red

object SessionsBrain {
    fun getStatus(session: Session? = null): SessionStatus {
        if (session == null) {
            return SessionStatus.NOT_DONE
        }

        val score = getScore(session)
        return when {
            score > 0.8 -> SessionStatus.GOOD
            score > 0.5 -> SessionStatus.ALMOST
            else -> SessionStatus.BAD
        }
    }

    // Max 1
    private fun getScore(session: Session): Double = when {
        session.phasesEnded > 0 && session.skipCount == 2 -> 0.6
        session.phasesEnded > 0 && session.skipCount <= 2 -> 1.0
        session.phasesEnded <= 0 -> 0.0
        else -> ((session.phasesEnded - session.skipCount) % 10) / 10.0
    }
}

enum class SessionStatus {
    GOOD,
    ALMOST,
    BAD,
    NOT_DONE
}

fun Session.getStatus(): SessionStatus {
    return SessionsBrain.getStatus(this)
}

fun SessionStatus.getColor() = when (this) {
    SessionStatus.GOOD -> Primary
    SessionStatus.ALMOST -> Orange
    SessionStatus.BAD -> Red
    SessionStatus.NOT_DONE -> Color.DarkGray
}
