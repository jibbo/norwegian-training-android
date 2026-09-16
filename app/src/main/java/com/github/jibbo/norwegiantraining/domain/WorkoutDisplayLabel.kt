package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Workout

/** Returns the shared name label used throughout workout surfaces. */
fun Workout.displayLabel(): String = name
