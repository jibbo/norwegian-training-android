package com.github.jibbo.norwegiantraining.log

import android.os.Bundle
import android.os.Build
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.DarkPrimary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@AndroidEntryPoint
class LogActivity : BaseActivity() {

    private val viewModel: LogViewModel by viewModels()
    private var todayStatsUiState = mutableStateOf<TodayStatsUiState>(TodayStatsUiState.Loading)

    @Inject lateinit var settingsRepository: SettingsRepository

    private val healthPermissionsLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        loadTodayStats()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NorwegianTrainingTheme(darkTheme = true) {
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
                    val uiState = viewModel.uiState.collectAsState()
                    when (uiState.value) {
                        is UiState.Loading -> {
                            CircularProgressIndicator()
                        }

                        is UiState.Loaded -> {
                            Logs(
                                innerPadding = innerPadding,
                                uiState = uiState.value as UiState.Loaded,
                                todayStatsUiState = todayStatsUiState.value,
                                onHideTodayStats = {
                                    settingsRepository.setShowTodayStatsInActivitySection(false)
                                    todayStatsUiState.value = TodayStatsUiState.Hidden
                                },
                                onRequestPermissions = { healthPermissionsLauncher.launch(requiredHealthPermissions()) },
                                onOpenHealthConnect = { openHealthConnectStore() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTodayStats()
    }

    private fun loadTodayStats() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (!settingsRepository.getShowTodayStatsInActivitySection()) {
            todayStatsUiState.value = TodayStatsUiState.Hidden
            return
        }

        todayStatsUiState.value = TodayStatsUiState.Loading

        val sdkStatus = HealthConnectClient.getSdkStatus(this)
        if (sdkStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            todayStatsUiState.value = TodayStatsUiState.InstallHealthConnect
            return
        }
        if (sdkStatus != HealthConnectClient.SDK_AVAILABLE && Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            todayStatsUiState.value = TodayStatsUiState.Stats(0L)
            return
        }

        val client = HealthConnectClient.getOrCreate(this)
        lifecycleScope.launch {
            val grantedPermissions = client.permissionController.getGrantedPermissions()
            val requiredPermissions = requiredHealthPermissions()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && !grantedPermissions.containsAll(requiredPermissions)) {
                todayStatsUiState.value = TodayStatsUiState.RequestHealthConnectPermissions
                return@launch
            }

            runCatching {
                val now = ZonedDateTime.now()
                val startOfDay = now.toLocalDate().atStartOfDay(now.zone)
                val endOfDay = startOfDay.plusDays(1)
                client.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startOfDay.toInstant(), endOfDay.toInstant())
                    )
                )[StepsRecord.COUNT_TOTAL] ?: 0L
            }.onSuccess { steps ->
                todayStatsUiState.value = TodayStatsUiState.Stats(steps)
            }.onFailure {
                todayStatsUiState.value = TodayStatsUiState.Stats(0L)
            }
        }
    }

    private fun requiredHealthPermissions(): Set<String> =
        setOf(HealthPermission.getReadPermission(StepsRecord::class))

    private fun openHealthConnectStore() {
        val packageUri = Uri.parse("market://details?id=com.google.android.apps.healthdata")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, packageUri))
        }.getOrElse {
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

}
