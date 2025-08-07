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
            session.phasesEnded == 0 -> SessionStatus.NOT_DONE
            session.phasesEnded > 3 && session.skipCount <= 1 -> SessionStatus.GOOD
            session.phasesEnded < 5 && session.skipCount <= 3 -> SessionStatus.ALMOST
            else -> SessionStatus.BAD
        }
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
    SessionStatus.ALMOST -> Color.Yellow
    SessionStatus.BAD -> Red
    SessionStatus.NOT_DONE -> Color.DarkGray
}
