package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FitnessLevelTest {
    @Test
    fun mapsBetweenFitnessLevelAndDifficulty() {
        assertEquals(Difficulty.BEGINNER, FitnessLevel.BEGINNER.toDifficulty())
        assertEquals(Difficulty.INTERMEDIATE, FitnessLevel.OCCASIONAL.toDifficulty())
        assertEquals(Difficulty.EXPERT, FitnessLevel.FIT.toDifficulty())

        assertEquals(FitnessLevel.BEGINNER, FitnessLevel.fromDifficulty(Difficulty.BEGINNER))
        assertEquals(FitnessLevel.OCCASIONAL, FitnessLevel.fromDifficulty(Difficulty.INTERMEDIATE))
        assertEquals(FitnessLevel.FIT, FitnessLevel.fromDifficulty(Difficulty.EXPERT))
    }

    @Test
    fun nextReturnsExpectedLevel() {
        assertEquals(FitnessLevel.OCCASIONAL, FitnessLevel.BEGINNER.next())
        assertEquals(FitnessLevel.FIT, FitnessLevel.OCCASIONAL.next())
        assertNull(FitnessLevel.FIT.next())
    }
}
