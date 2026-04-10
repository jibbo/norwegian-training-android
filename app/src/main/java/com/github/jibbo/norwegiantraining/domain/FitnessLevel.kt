package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty

enum class FitnessLevel {
    BEGINNER,
    OCCASIONAL,
    FIT;

    fun toDifficulty(): Difficulty = when (this) {
        BEGINNER -> Difficulty.BEGINNER
        OCCASIONAL -> Difficulty.INTERMEDIATE
        FIT -> Difficulty.EXPERT
    }

    fun next(): FitnessLevel? = when (this) {
        BEGINNER -> OCCASIONAL
        OCCASIONAL -> FIT
        FIT -> null
    }
}
