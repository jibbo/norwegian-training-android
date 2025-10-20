package com.github.jibbo.norwegiantraining.paywall

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.data.SharedPreferencesSettingsRepository
import com.github.jibbo.norwegiantraining.main.MainActivity
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
class PaywallActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NorwegianTrainingTheme {
                Scaffold { _ ->
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
                                        goToMainActivityIfPaid(customerInfo)
                                    }

                                    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                                        goToMainActivityIfPaid(customerInfo)
                                    }
                                }
                            )
                            .setShouldDisplayDismissButton(false)
                            .build()
                    )
                }
            }
        }
    }

    fun goToMainActivityIfPaid(customerInfo: CustomerInfo) {
        if (customerInfo.entitlements.active.isNotEmpty()) {
            SharedPreferencesSettingsRepository(this).onboardingCompleted()
            startActivity(Intent(this@PaywallActivity, MainActivity::class.java))
        }
    }

}
