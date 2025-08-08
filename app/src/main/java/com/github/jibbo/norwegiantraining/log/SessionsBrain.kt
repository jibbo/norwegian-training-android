package com.github.jibbo.norwegiantraining.log

import androidx.compose.ui.graphics.Color
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Red

object SessionsBrain {
    fun getStatus(session: Session? = null): SessionStatus {
        if (session == null) {
            return SessionStatus.NOT_DONE
        }

        return when {
            getScore(session) > 0.8 -> SessionStatus.GOOD
            getScore(session) > 0.5 -> SessionStatus.ALMOST
            else -> SessionStatus.BAD
        }
    }

    // Max 1
    private fun getScore(session: Session): Int =
        (session.phasesEnded / (session.skipCount + 1)) / 10
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
    SessionStatus.ALMOST -> Color.Yellow
    SessionStatus.BAD -> Red
    SessionStatus.NOT_DONE -> Color.DarkGray
}
