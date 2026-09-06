package com.github.jibbo.norwegiantraining.log

import org.junit.Assert.assertEquals
import org.junit.Test

class TodayStatsDecisionTest {

    @Test
    fun `hidden when setting off`() {
        assertEquals(
            TodayStatsUiState.Hidden,
            decideTodayStatsUiState(
                showTodayStats = false,
                isResolving = false,
                steps = null,
                sdkInt = 34,
                healthConnectAvailability = HealthConnectAvailability.AVAILABLE,
                requiredPermissionsGranted = true,
            )
        )
    }

    @Test
    fun `loading when unresolved and enabled`() {
        assertEquals(
            TodayStatsUiState.Loading,
            decideTodayStatsUiState(
                showTodayStats = true,
                isResolving = true,
                steps = null,
                sdkInt = 34,
                healthConnectAvailability = HealthConnectAvailability.AVAILABLE,
                requiredPermissionsGranted = true,
            )
        )
    }

    @Test
    fun `stats zero when steps are zero`() {
        assertEquals(
            TodayStatsUiState.Stats(0L),
            decideTodayStatsUiState(
                showTodayStats = true,
                isResolving = false,
                steps = 0L,
                sdkInt = 34,
                healthConnectAvailability = HealthConnectAvailability.AVAILABLE,
                requiredPermissionsGranted = true,
            )
        )
    }

    @Test
    fun `install card on android below 14 when health connect missing`() {
        assertEquals(
            TodayStatsUiState.InstallHealthConnect,
            decideTodayStatsUiState(
                showTodayStats = true,
                isResolving = false,
                steps = null,
                sdkInt = 33,
                healthConnectAvailability = HealthConnectAvailability.MISSING,
                requiredPermissionsGranted = false,
            )
        )
    }

    @Test
    fun `permission card on android 14 plus when permissions missing`() {
        assertEquals(
            TodayStatsUiState.RequestHealthConnectPermissions,
            decideTodayStatsUiState(
                showTodayStats = true,
                isResolving = false,
                steps = null,
                sdkInt = 34,
                healthConnectAvailability = HealthConnectAvailability.AVAILABLE,
                requiredPermissionsGranted = false,
            )
        )
    }

    @Test
    fun `fallback to zero stats when unavailable`() {
        assertEquals(
            TodayStatsUiState.Stats(0L),
            decideTodayStatsUiState(
                showTodayStats = true,
                isResolving = false,
                steps = null,
                sdkInt = 34,
                healthConnectAvailability = HealthConnectAvailability.UNAVAILABLE,
                requiredPermissionsGranted = true,
            )
        )
    }

    @Test
    fun `steps win over prompt state`() {
        assertEquals(
            TodayStatsUiState.Stats(123L),
            decideTodayStatsUiState(
                showTodayStats = true,
                isResolving = false,
                steps = 123L,
                sdkInt = 34,
                healthConnectAvailability = HealthConnectAvailability.MISSING,
                requiredPermissionsGranted = false,
            )
        )
    }
}
