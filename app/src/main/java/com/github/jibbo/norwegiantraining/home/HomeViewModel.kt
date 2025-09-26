package com.github.jibbo.norwegiantraining.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.BuildConfig
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.domain.GetAllWorkouts
import com.github.jibbo.norwegiantraining.domain.GetUsername
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
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val events: MutableSharedFlow<UiCommands> = MutableSharedFlow()
    val uiEvents = events.asSharedFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(
        UiState(username = getUsername(), workouts = hashMapOf())
    )
    val uiStates = states.asStateFlow()

    fun refresh() {
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = {
                // TODO handle error
            },
            onSuccess = purchasedCheck()
        )
        viewModelScope.launch {
            if (!settingsRepository.isOnboardingCompleted() && !BuildConfig.DEBUG) {
                events.emit(UiCommands.SHOW_ONBOARDING)
            }
            states.value = states.value.copy(
                //TODO this should be moved to datastore for Flow usage and avoid this workaround
                username = getUsername(),
                workouts = getAllWorkouts()
            )
        }
    }

    fun settingsClicked() {
        publishEvent(UiCommands.SHOW_SETTINGS)
    }

    fun chartsClicked() {
        publishEvent(UiCommands.SHOW_CHARTS)
    }

    private fun publishEvent(uiCommand: UiCommands) {
        viewModelScope.launch {
            events.emit(uiCommand)
        }
    }

    private fun purchasedCheck(): (CustomerInfo) -> Unit = { customerInfo ->
        val hasNotPurchased = customerInfo.entitlements.active.isEmpty()
        if (hasNotPurchased && !BuildConfig.DEBUG) {
            viewModelScope.launch {
                events.emit(UiCommands.SHOW_PAYWALL)
            }
        }

    }

    fun workoutClicked(id: Long) {
        viewModelScope.launch {
            events.emit(UiCommands.SHOW_WORKOUT(id))
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
