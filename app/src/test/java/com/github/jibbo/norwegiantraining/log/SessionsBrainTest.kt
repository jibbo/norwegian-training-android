package com.github.jibbo.norwegiantraining.log

import com.github.jibbo.norwegiantraining.data.Session
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionsBrainTest {
    @Test
    fun classifiesSessionStatusFromScore() {
        assertEquals(SessionStatus.NOT_DONE, SessionsBrain.getStatus(null))
        assertEquals(SessionStatus.BAD, SessionsBrain.getStatus(Session(phasesEnded = 0, skipCount = 0, date = Date())))
        assertEquals(SessionStatus.GOOD, SessionsBrain.getStatus(Session(phasesEnded = 5, skipCount = 0, date = Date())))
        assertEquals(SessionStatus.ALMOST, SessionsBrain.getStatus(Session(phasesEnded = 1, skipCount = 2, date = Date())))
        assertEquals(SessionStatus.BAD, SessionsBrain.getStatus(Session(phasesEnded = 1, skipCount = 4, date = Date())))
    }

    @Test
    fun logNameIsLimitedToTwentyCharactersWithOneEllipsis() {
        val session = Session(name = "123456789012345678901234")

        assertEquals("12345678901234567890…", session.logName())
    }

    @Test
    fun identifiedSessionsShowSkippedPhasesForAlmostAndBadIncludingZero() {
        assertEquals(
            LogDetails.SkippedPhasesAndCalories,
            Session(workoutId = 1L, phasesEnded = 0, skipCount = 0).logDetails(),
        )
        assertEquals(
            LogDetails.SkippedPhasesAndCalories,
            Session(workoutId = 1L, phasesEnded = 1, skipCount = 4).logDetails(),
        )
    }
}
