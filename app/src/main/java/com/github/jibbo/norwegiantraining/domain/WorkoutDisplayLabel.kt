package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Workout

/** Returns the shared icon/name label used throughout workout surfaces. */
fun Workout.displayLabel(): String =
    if (icon.isNullOrEmpty()) name else "$icon $name"
