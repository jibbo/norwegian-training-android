package com.github.jibbo.norwegiantraining.domain

enum class PhaseName {
    GET_READY,
    WARMUP,
    HARD_PHASE,
    SOFT_PHASE,
    REST_PHASE,
    COMPLETED;
}

data class Phase(val name: PhaseName, val durationMillis: Long)
