package com.github.jibbo.norwegiantraining.domain

enum class Phase(val durationMillis: Long? = null) {
    GET_READY(),
    WARMUP(10 * 60 * 1000), // 10 minutes
    HARD_PHASE(4 * 60 * 1000), // 4 minutes
    SOFT_PHASE(4 * 60 * 1000), // 4 minutes
    REST_PHASE(5 * 60 * 1000), // 5 minutes
    COMPLETED();

    companion object {
        fun fromNumber(number: Int) = when {
            number == 0 -> GET_READY
            number == 1 -> WARMUP
            number == 10 -> REST_PHASE
            number == 11 -> COMPLETED
            number % 2 == 0 -> HARD_PHASE
            number % 2 == 1 -> SOFT_PHASE
            else -> throw IllegalArgumentException("Invalid number: $number")
        }
    }
}
