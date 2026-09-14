@file:SuppressLint("NewApi")

package com.github.jibbo.norwegiantraining.log

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.AnimatedToolbar
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.Gray
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Logs(
    innerPadding: PaddingValues,
    uiState: UiState.Loaded,
    todayStatsUiState: TodayStatsUiState,
    onOpenManualWorkout: (LocalDate) -> Unit,
    onHideTodayStats: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenHealthConnect: () -> Unit,
) {
    val listState = rememberLazyListState()
    var selectedDay by remember { mutableStateOf<Date?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(
            top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding(),
        ),
    ) {
        AnimatedToolbar(
            R.string.title_activity_logs.localizable(), listState, null,
            trailingContent = {
                IconButton(
                    onClick = { onOpenManualWorkout(LocalDate.now()) },
                    modifier = Modifier.testTag("today"),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_calendar_today_24),
                        contentDescription = "",
                    )
                }
            },
        )
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            item { TodayStatsArea(todayStatsUiState, onHideTodayStats, onRequestPermissions, onOpenHealthConnect) }
            if (todayStatsUiState is TodayStatsUiState.Stats) {
                item { Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StepsCard(todayStatsUiState.steps)
                    DistanceCard(todayStatsUiState.steps)
                } }
            }
            items(12) { month -> Month(month, uiState) { selectedDay = it } }
        }
    }
    selectedDay?.let { day ->
        val sessionsForDay = uiState.logs.values.filterNotNull().flatten().filter { it.date.isSameDay(day) }
        ModalBottomSheet(
            onDismissRequest = { selectedDay = null }, modifier = Modifier.testTag("sessions_for_day_sheet"),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(SimpleDateFormat("MMMM d").format(day), style = Typography.headlineSmall)
                    IconButton(
                        onClick = {
                            selectedDay = null
                            onOpenManualWorkout(day.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        }, modifier = Modifier.testTag("add_manual_workout_for_day"),
                    ) { Text("+", style = Typography.headlineSmall) }
                }
                Spacer(Modifier.height(16.dp))
                sessionsForDay.forEach { session ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.SpaceBetween) {
                        Text(session.name, style = Typography.bodyLarge)
                        Text(R.string.workout_time.localizable(session.duration.toString()), style = Typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TodayStatsArea(state: TodayStatsUiState, onHide: () -> Unit, onRequest: () -> Unit, onOpen: () -> Unit) {
    when (state) {
        TodayStatsUiState.Hidden -> Unit
        TodayStatsUiState.Loading -> ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Gray)) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        is TodayStatsUiState.Stats -> CaloriesCard(state.steps)
        TodayStatsUiState.InstallHealthConnect -> HealthConnectCard(R.string.health_connect_install_message.localizable(), onOpen, onHide, "today_stats_install_health_connect_card")
        TodayStatsUiState.RequestHealthConnectPermissions -> HealthConnectCard(R.string.health_connect_permission_message.localizable(), onRequest, onHide, "today_stats_permissions_card")
    }
}

@Composable
private fun HealthConnectCard(message: String, onCardClick: () -> Unit, onHide: () -> Unit, tag: String) {
    ElevatedCard(Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag(tag), colors = CardDefaults.elevatedCardColors(containerColor = Gray)) {
        Column(Modifier.padding(16.dp)) {
            Text(R.string.health_connect_title.localizable(), style = Typography.headlineSmall, color = Primary)
            Spacer(Modifier.height(12.dp)); Text(message, style = Typography.bodyMedium, color = White)
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                androidx.compose.material3.TextButton(onClick = onHide) { Text(R.string.hide.localizable()) }
                androidx.compose.material3.TextButton(onClick = onCardClick) { Text(R.string.ok.localizable()) }
            }
        }
    }
}

@Composable private fun StepsCard(steps: Long?) { MetricCard(steps?.toString() ?: "0", R.string.steps_today.localizable(), "steps_card") }
@Composable private fun DistanceCard(steps: Long?) { MetricCard("%.2f".format(steps?.times(0.70)?.div(1000) ?: 0.0), R.string.distance_today.localizable(), "distance_card") }
@Composable private fun CaloriesCard(steps: Long?) { MetricCard("%.2f".format(steps?.times(0.044) ?: 0.0), R.string.calories_burned.localizable(), "calories_card") }
@Composable private fun MetricCard(value: String, label: String, tag: String) {
    ElevatedCard(Modifier.padding(bottom = 16.dp).testTag(tag), colors = CardDefaults.elevatedCardColors(containerColor = Gray)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
            Text(value, style = Typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
            Text(label, style = Typography.headlineSmall, color = White)
        }
    }
}

@Composable private fun Month(month: Int, uiState: UiState.Loaded, onDayClick: (Date) -> Unit) {
    val calendar = Calendar.getInstance().apply { set(Calendar.MONTH, month) }
    Text(SimpleDateFormat("MMMM").format(calendar.time).capitalizeFirstLetter(), Modifier.padding(horizontal = 4.dp))
    FlowRow(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) Day(calendar, i, uiState, month, onDayClick)
    }
    Spacer(Modifier.fillMaxWidth().height(32.dp))
}

@Composable private fun Day(calendar: Calendar, index: Int, uiState: UiState.Loaded, month: Int, onDayClick: (Date) -> Unit) {
    calendar.set(Calendar.DAY_OF_MONTH, index); calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
    val date = calendar.time
    val item = uiState.logs[month]?.find { it.date.isSameDay(date) }
    val modifier = Modifier.size(32.dp).padding(4.dp).clip(CircleShape).clickable { onDayClick(date) }.testTag("calendar_day_${month}_$index")
    if (item == null) Text(index.toString(), textAlign = TextAlign.Center, modifier = modifier.fillMaxSize())
    else Box(modifier.background(item.getStatus().getColor()))
}

private fun Date.isSameDay(other: Date): Boolean {
    val first = Calendar.getInstance().apply { time = this@isSameDay }
    val second = Calendar.getInstance().apply { time = other }
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

private fun String.capitalizeFirstLetter() = if (isNotEmpty()) this[0].uppercase() + substring(1) else this

@Composable
@androidx.compose.ui.tooling.preview.Preview
private fun Preview() {
    NorwegianTrainingTheme { Logs(PaddingValues(), UiState.Loaded(emptyMap()), TodayStatsUiState.Hidden, {}, {}, {}, {}) }
}
