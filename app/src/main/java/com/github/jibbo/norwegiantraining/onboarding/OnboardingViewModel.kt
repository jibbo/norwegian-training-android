package com.github.jibbo.norwegiantraining.onboarding

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    fun continueClicked() {
        TODO()
        /*
        if (page == states.size - 1) {
                    val intent = Intent(
                        current,
                        getNextActivity(hasPaid, settingsRepository)
                    )
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    current.startActivity(intent)
                }
                if (state is OnboardingPage.Permission) {
                    if (ContextCompat.checkSelfPermission(
                            current,
                            state.permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        launcher.launch(state.permission)
                        return@Button
                    }
                }
                coroutineScope.launch {
                    pagerState.scrollToPage(page + 1)
                }
         */
        /*
        private fun getNextActivity(
    hasNotPaid: MutableState<Boolean>,
    settingsRepository: SettingsRepository
): Class<out BaseActivity> =
    if (BuildConfig.DEBUG) {
        HomeActivity::class.java
    } else if (hasNotPaid.value) {
        PaywallActivity::class.java
    } else {
        settingsRepository.onboardingCompleted()
        HomeActivity::class.java
    }
         */
    }

    private val events: MutableSharedFlow<UiCommands> = MutableSharedFlow()
    val uiEvents = events.asSharedFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiStates = states.asStateFlow()

    init {
        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            val hasPaid = customerInfo.entitlements.active.isEmpty()
            states.value = UiState.Show(hasPaid, OnboardingStates.states())
        }
    }

    object OnboardingStates {
        private val states = mutableListOf(
//        UiState.Feedback(
//            title = R.string.onboarding_step_1_title,
//            body = R.string.onboarding_step_1_body,
//            description = R.string.onboarding_step_1_description,
//            name = R.string.onboarding_step_1_name,
//            handle = R.string.onboarding_step_1_handle,
//            image = R.mipmap.profile_pic
//        ),
            OnboardingPage.Normal(
                title = R.string.onboarding_step_6_title,
                description = R.string.onboarding_step_6_description,
                body = R.string.onboarding_step_6_body,
                image = R.drawable.runner_illustration
            ),
            OnboardingPage.Normal(
                title = R.string.onboarding_step_5_title,
                description = R.string.onboarding_step_5_description,
                body = R.string.onboarding_step_5_body,
                image = R.drawable.organizing_data_illustration
            ),
            OnboardingPage.Normal(
                title = R.string.onboarding_step_2_title,
                description = R.string.onboarding_step_2_description,
                body = R.string.onboarding_step_2_body,
                image = R.drawable.working_out_illustration
            ),

            OnboardingPage.Permission(
                title = R.string.onboarding_step_notification_permission_title,
                image = R.drawable.processing_perm_illustration,
                body = R.string.onboarding_step_notification_permission_body,
                permission = Manifest.permission.POST_NOTIFICATIONS
            ),
            OnboardingPage.Permission(
                title = R.string.onboarding_step_activity_recognition_permission_title,
                image = R.drawable.physical_perm_illustration,
                body = R.string.onboarding_step_activity_recognition_permission_body,
                permission = Manifest.permission.ACTIVITY_RECOGNITION
            ),
//        UiState.Questions(
//            title = R.string.onboarding_step_3_title,
//            options = arrayOf(
//                R.string.onboarding_step_3_option_1,
//                R.string.onboarding_step_3_option_2
//            )
//        ),
            OnboardingPage.Normal(
                title = R.string.onboarding_step_4_title,
                description = R.string.onboarding_step_4_description,
                body = R.string.onboarding_step_4_body,
                image = R.drawable.hearth_illustration
            ),
        )

        fun states() = states
            .filterNot {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                        it is OnboardingPage.Permission &&
                        it.permission == Manifest.permission.POST_NOTIFICATIONS
            }
            .filterNot {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                        it is OnboardingPage.Permission &&
                        it.permission == Manifest.permission.ACTIVITY_RECOGNITION
            }
    }
}

sealed class UiCommands {
    object SHOW_HOME : UiCommands()
    object SHOW_PAYWALL : UiCommands()
}