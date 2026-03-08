package com.github.jibbo.norwegiantraining.paywall

import android.content.Intent
import android.os.Bundle
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.data.SharedPreferencesSettingsRepository
import com.github.jibbo.norwegiantraining.freetrial.FreeTrialActivity
import com.github.jibbo.norwegiantraining.home.HomeActivity
import com.github.jibbo.norwegiantraining.onboarding.OnboardingActivity
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import kotlinx.coroutines.flow.Flow
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
class PaywallActivity : BaseActivity() {
    lateinit var sharedPreferencesSettingsRepository: SharedPreferencesSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferencesSettingsRepository = SharedPreferencesSettingsRepository(this)
        val freeTrialEndDate = sharedPreferencesSettingsRepository.getFreeTrialEndDate()

        setContent {
            NorwegianTrainingTheme {
                Scaffold { padding ->
                    Paywall(
                        options = PaywallOptions.Builder(
                            dismissRequest = {},
                        )
                            .setListener(
                                object : PaywallListener {
                                    override fun onPurchaseCompleted(
                                        customerInfo: CustomerInfo,
                                        storeTransaction: StoreTransaction
                                    ) {
                                        goToMainActivityIfPaid(customerInfo, freeTrialEndDate)
                                    }

                                    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                                        goToMainActivityIfPaid(customerInfo, freeTrialEndDate)
                                    }

                                    override fun onPurchaseCancelled() {
                                        super.onPurchaseCancelled()
                                        goToMainActivityIfPaid(null, freeTrialEndDate)
                                    }
                                }
                            )
                            .setShouldDisplayDismissButton(false)
                            .build()
                    )
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = {
                            goToMainActivityIfPaid(null, freeTrialEndDate)
                        }, modifier = Modifier.padding(vertical = padding.calculateTopPadding())) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.outline_close_24
                                ), contentDescription = "Close"
                            )
                        }
                    }

                    PredictiveBackHandler(enabled = true) { progress: Flow<BackEventCompat> ->
                        try {
                            progress.collect { backEvent ->
                                // Update your UI or animation based on backEvent.progress
                            }
                            // Handle the final back action (e.g., navigate back)
                            goToMainActivityIfPaid(null, freeTrialEndDate)
                        } catch (e: CancellationException) {
                            // Back gesture was cancelled, reset your UI
                        }
                    }
                }
            }
        }
    }

    fun goToMainActivityIfPaid(customerInfo: CustomerInfo?, freeTrialEndDate: Date?) {
        val activityToBeOpened = when {
            customerInfo?.entitlements?.active?.isNotEmpty() == true -> {
                HomeActivity::class.java
            }

            freeTrialEndDate == null -> {
                FreeTrialActivity::class.java
            }

            freeTrialEndDate.after(Date()) -> {
                HomeActivity::class.java
            }

            else -> {
                OnboardingActivity::class.java
            }
        }
        sharedPreferencesSettingsRepository.onboardingCompleted()
        startActivity(Intent(this@PaywallActivity, activityToBeOpened))
    }
}
