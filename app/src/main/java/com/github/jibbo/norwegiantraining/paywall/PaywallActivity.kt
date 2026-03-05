package com.github.jibbo.norwegiantraining.paywall

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.BuildConfig
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.data.SharedPreferencesSettingsRepository
import com.github.jibbo.norwegiantraining.freetrial.FreeTrialActivity
import com.github.jibbo.norwegiantraining.home.HomeActivity
import com.github.jibbo.norwegiantraining.onboarding.OnboardingActivity
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.DarkPrimary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import java.util.Date

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
class PaywallActivity : BaseActivity() {
    lateinit var sharedPreferencesSettingsRepository: SharedPreferencesSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferencesSettingsRepository = SharedPreferencesSettingsRepository(this)
        val freeTrialEndDate = sharedPreferencesSettingsRepository.getFreeTrialEndDate()

        setContent {
            NorwegianTrainingTheme {
                Scaffold { _ ->
                    if (!BuildConfig.DEBUG) {
                        Paywall(
                            options = PaywallOptions.Builder(
                                dismissRequest = {
                                    goToMainActivityIfPaid(null, freeTrialEndDate)
                                },
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
                                    }
                                )
                                .setShouldDisplayDismissButton(false)
                                .build()
                        )
                    } else {
                        DebugPayWall()
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

    @Composable
    fun DebugPayWall() {
        NorwegianTrainingTheme {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Black,
                                DarkPrimary
                            )
                        )
                    )
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                ) {
                    Button(onClick = {
                        val date = Date().apply {
                            time += 24 * 60 * 60 * 1000
                        }
                        goToMainActivityIfPaid(null, date)
                    }) {
                        Text(
                            text = "TRIAL OK",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Button(onClick = {
                        goToMainActivityIfPaid(null, Date())
                    }) {
                        Text(
                            text = "EXPIRED TRIAL",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Button(onClick = {
                        goToMainActivityIfPaid(null, null)
                    }) {
                        Text(
                            text = "NEW TRIAL",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Button(onClick = {
                        sharedPreferencesSettingsRepository.debugOnlySetFreeTrialDate(null)
                        startActivity(Intent(this@PaywallActivity, HomeActivity::class.java))
                    }) {
                        Text(
                            text = "PURCHASE",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    @Preview
    fun Preview() {
        DebugPayWall()
    }
}
