package com.github.jibbo.norwegiantraining.home

import com.github.jibbo.norwegiantraining.data.Workout

fun canLaunchWorkout(
    workout: Workout,
    isFreeTrial: Boolean,
    hasEntitlement: Boolean,
): Boolean = workout.isCustom || isFreeTrial || hasEntitlement
