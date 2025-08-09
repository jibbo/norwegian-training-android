package com.github.jibbo.norwegiantraining.domain

enum class Phase(val durationMillis: Long? = null) {
    GET_READY(),
    WARMUP(10 * 60 * 1000), // 10 minutes
    HARD_PHASE(4 * 60 * 1000), // 4 minutes
    SOFT_PHASE(4 * 60 * 1000), // 4 minutes
    REST_PHASE(5 * 60 * 1000), // 5 minutes
    COMPLETED();

    companion object {
        private val phases = arrayOf(
            GET_READY,
            WARMUP,
            HARD_PHASE,
            SOFT_PHASE,
            HARD_PHASE,
            SOFT_PHASE,
            HARD_PHASE,
            SOFT_PHASE,
            HARD_PHASE,
            SOFT_PHASE,
            REST_PHASE,
            COMPLETED
        )

        fun fromNumber(number: Int) = phases[number % phases.size]
    }
}
