package com.github.jibbo.norwegiantraining.log

enum class HealthConnectAvailability {
    AVAILABLE,
    MISSING,
    UPDATE_REQUIRED,
    UNAVAILABLE
}

sealed interface TodayStatsUiState {
    data object Hidden : TodayStatsUiState
    data object Loading : TodayStatsUiState
    data class Stats(val steps: Long) : TodayStatsUiState
    data object InstallHealthConnect : TodayStatsUiState
    data object RequestHealthConnectPermissions : TodayStatsUiState
}

fun decideTodayStatsUiState(
    showTodayStats: Boolean,
    isResolving: Boolean,
    steps: Long?,
    sdkInt: Int,
    healthConnectAvailability: HealthConnectAvailability,
    requiredPermissionsGranted: Boolean,
): TodayStatsUiState {
    if (!showTodayStats) return TodayStatsUiState.Hidden
    steps?.let { return TodayStatsUiState.Stats(it) }
    if (isResolving) return TodayStatsUiState.Loading

    return when {
        sdkInt < 34 && healthConnectAvailability == HealthConnectAvailability.MISSING -> {
            TodayStatsUiState.InstallHealthConnect
        }
        sdkInt >= 34 && !requiredPermissionsGranted -> {
            TodayStatsUiState.RequestHealthConnectPermissions
        }
        else -> TodayStatsUiState.Stats(0L)
    }
}
