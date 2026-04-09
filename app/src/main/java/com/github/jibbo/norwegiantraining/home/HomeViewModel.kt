package com.github.jibbo.norwegiantraining.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.domain.GetAllWorkouts
import com.github.jibbo.norwegiantraining.domain.GetRecommendedWorkoutId
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.domain.isFreeTrial
import com.github.jibbo.norwegiantraining.domain.isOnboardingComplete
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUsername: GetUsername,
    private val getAllWorkouts: GetAllWorkouts,
    private val isFreeTrial: isFreeTrial,
    private val isOnboardingComplete: isOnboardingComplete,
    private val getRecommendedWorkoutId: GetRecommendedWorkoutId,
    private val analytics: Analytics,
) : ViewModel() {

    private val events: MutableSharedFlow<UiCommands> = MutableSharedFlow()
    val uiEvents = events.asSharedFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiStates = states.asStateFlow()

    private val isTrial = isFreeTrial()

    init {
        viewModelScope.launch {
            if (!isOnboardingComplete()) {
                events.emit(UiCommands.SHOW_ONBOARDING)
            } else {
                getAllWorkouts().collect { workoutsMap ->
                    showWorkouts(workoutsMap)
                }
            }
        }
    }

    fun refresh() {
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { pError ->
                analytics.logRevenueCatError(pError.code.name, pError.message)
            },
            onSuccess = purchasedCheck()
        )
        refreshUsername()
    }

    fun settingsClicked() {
        publishEvent(UiCommands.SHOW_SETTINGS)
    }

    fun chartsClicked() {
        publishEvent(UiCommands.SHOW_CHARTS)
    }

    fun workoutClicked(id: Long) {
        viewModelScope.launch {
            when {
                isTrial -> {
                    events.emit(UiCommands.SHOW_WORKOUT(id))
                }

                else -> {
                    events.emit(UiCommands.SHOW_WORKOUT(id))
                }
            }
        }
    }

    private fun publishEvent(uiCommand: UiCommands) {
        viewModelScope.launch {
            events.emit(uiCommand)
        }
    }

    private fun purchasedCheck(): (CustomerInfo) -> Unit = { customerInfo ->
        val hasNotPurchased = customerInfo.entitlements.active.isEmpty()
        if (hasNotPurchased) {
            if (!isTrial) {
                viewModelScope.launch {
                    events.emit(UiCommands.SHOW_PAYWALL)
                }
            }
        }
    }

    private fun showWorkouts(workouts: Map<Difficulty, List<Workout>>) =
        when (val value = states.value) {
            is UiState.Loaded -> {
                states.value = value.copy(workouts = workouts)
            }

            else -> states.value = UiState.Loaded(
                username = getUsername(),
                workouts = workouts,
                recommendedWorkoutId = getRecommendedWorkoutId(workouts)
            )
        }

    private fun refreshUsername() {
        val value = states.value
        if (value is UiState.Loaded) {
            states.value = value.copy(username = getUsername())
        }
    }
}

sealed class UiCommands {
    object SHOW_SETTINGS : UiCommands()
    object SHOW_CHARTS : UiCommands()
    object SHOW_ONBOARDING : UiCommands()
    object SHOW_PAYWALL : UiCommands()
    data class SHOW_WORKOUT(val id: Long) : UiCommands()
}
