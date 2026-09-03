package com.github.jibbo.norwegiantraining.log

import android.os.Bundle
import android.os.Build
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
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.DarkPrimary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@AndroidEntryPoint
class LogActivity : BaseActivity() {

    private val viewModel: LogViewModel by viewModels()
    private var todaySteps = mutableStateOf<Long?>(null)
    private var hasRequestedHealthPermission = false

    private val healthPermissionsLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (HealthPermission.getReadPermission(StepsRecord::class) in grantedPermissions) {
            loadTodaySteps()
        }
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
                                todaySteps = todaySteps.value
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTodaySteps()
    }

    private fun loadTodaySteps() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (HealthConnectClient.getSdkStatus(this) != HealthConnectClient.SDK_AVAILABLE) return

        val client = HealthConnectClient.getOrCreate(this)
        lifecycleScope.launch {
            val readStepsPermission = HealthPermission.getReadPermission(StepsRecord::class)
            if (readStepsPermission !in client.permissionController.getGrantedPermissions()) {
                if (!hasRequestedHealthPermission) {
                    hasRequestedHealthPermission = true
                    healthPermissionsLauncher.launch(setOf(readStepsPermission))
                }
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
                todaySteps.value = steps
            }
        }
    }

}
