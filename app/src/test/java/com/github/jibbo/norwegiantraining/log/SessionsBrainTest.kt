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
}
