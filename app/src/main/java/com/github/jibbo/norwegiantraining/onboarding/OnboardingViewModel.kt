package com.github.jibbo.norwegiantraining.onboarding

import android.Manifest
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.BuildConfig
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.data.SettingsRepository
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
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val events: MutableSharedFlow<UiCommands> = MutableSharedFlow(replay = 1)
    val uiEvents = events.asSharedFlow()

    private val selectedPage = MutableStateFlow(0)
    val uiSelectedPage = selectedPage.asStateFlow()

    private val name = MutableStateFlow("")
    val uiName = name.asStateFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(
        UiState.Show(OnboardingStates.getOnboardingPages())
    )
    val uiStates = states.asStateFlow()
    fun continueClicked(step: Int) {
        val onboardingPages = OnboardingStates.getOnboardingPages()
        if (onboardingPages[step] is OnboardingPage.Permission) {
            val state = onboardingPages[step] as OnboardingPage.Permission
            viewModelScope.launch {
                events.emit(UiCommands.AskPermission(state.permission))
            }
        }
        if (step == onboardingPages.size - 1) {
            if (BuildConfig.DEBUG) {
                showHome()
            } else if (Purchases.isConfigured) {
                Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
                    val hasPaid = customerInfo.entitlements.active.isNotEmpty()
                    if (hasPaid) {
                        showHome()

                    } else {
                        viewModelScope.launch {
                            events.emit(UiCommands.SHOW_PAYWALL)
                        }
                    }
                }
            }
        } else {
            showNextPage()
        }
    }

    private fun showNextPage() {
        selectedPage.value += 1
//        viewModelScope.launch {
//            val value = states.value as UiState.Show
//            states.value = value.copy(selectedPage = selectedPage)
//        }
    }

    fun permissionResult(granted: Boolean) {
        showNextPage()
    }

    private fun showHome() {
        settingsRepository.onboardingCompleted()
        viewModelScope.launch {
            events.emit(UiCommands.SHOW_HOME)
        }
    }

    fun onNameChanged(newName: String) {
        name.value = newName
        settingsRepository.setUserName(newName)
    }

    fun onPageSwiped(page: Int) {
        selectedPage.value = page
    }

    object OnboardingStates {
        fun getOnboardingPages(): List<OnboardingPage> = buildList {
            add(
                OnboardingPage.Normal(
                    title = R.string.onboarding_step_6_title,
                    description = R.string.onboarding_step_6_description,
                    body = R.string.onboarding_step_6_body,
                    image = R.drawable.runner_illustration
                )
            )
            add(
                OnboardingPage.Normal(
                    title = R.string.onboarding_step_5_title,
                    description = R.string.onboarding_step_5_description,
                    body = R.string.onboarding_step_5_body,
                    image = R.drawable.organizing_data_illustration
                )
            )
            add(
                OnboardingPage.Normal(
                    title = R.string.onboarding_step_2_title,
                    description = R.string.onboarding_step_2_description,
                    body = R.string.onboarding_step_2_body,
                    image = R.drawable.working_out_illustration
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(
                    OnboardingPage.Permission(
                        title = R.string.onboarding_step_activity_recognition_permission_title,
                        image = R.drawable.physical_perm_illustration,
                        body = R.string.onboarding_step_activity_recognition_permission_body,
                        permission = Manifest.permission.ACTIVITY_RECOGNITION
                    )
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    OnboardingPage.Permission(
                        title = R.string.onboarding_step_notification_permission_title,
                        image = R.drawable.processing_perm_illustration,
                        body = R.string.onboarding_step_notification_permission_body,
                        permission = Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            }

            add(
                OnboardingPage.NameSetting(
                    title = R.string.onboarding_step_name_title,
                    placeholder = R.string.onboarding_step_name_placeholder,
                )
            )

            add(
                OnboardingPage.Normal(
                    title = R.string.onboarding_step_4_title,
                    description = R.string.onboarding_step_4_description,
                    body = R.string.onboarding_step_4_body,
                    image = R.drawable.hearth_illustration
                )
            )
        }
    }
}

sealed class UiCommands {
    object SHOW_HOME : UiCommands()
    object SHOW_PAYWALL : UiCommands()

    data class AskPermission(val permission: String) : UiCommands()
}