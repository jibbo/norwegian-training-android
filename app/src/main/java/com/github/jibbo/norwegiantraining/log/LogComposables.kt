package com.github.jibbo.norwegiantraining.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.AnimatedToolbar
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutType
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutUiState
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutField
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutValidationError
import com.github.jibbo.norwegiantraining.log.TodayStatsUiState.Hidden
import com.github.jibbo.norwegiantraining.log.TodayStatsUiState.InstallHealthConnect
import com.github.jibbo.norwegiantraining.log.TodayStatsUiState.Loading
import com.github.jibbo.norwegiantraining.log.TodayStatsUiState.RequestHealthConnectPermissions
import com.github.jibbo.norwegiantraining.log.TodayStatsUiState.Stats
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.Gray
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Logs(
    innerPadding: PaddingValues,
    uiState: UiState.Loaded,
    todayStatsUiState: TodayStatsUiState,
    manualWorkoutUiState: ManualWorkoutUiState,
    onOpenManualWorkout: () -> Unit,
    onDismissManualWorkout: () -> Unit,
    onSelectManualWorkoutType: (ManualWorkoutType?) -> Unit,
    onUpdateManualWorkoutDate: (LocalDate) -> Unit,
    onUpdateManualWorkoutHours: (String) -> Unit,
    onUpdateManualWorkoutMinutes: (String) -> Unit,
    onSubmitManualWorkout: (String) -> Unit,
    onHideTodayStats: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenHealthConnect: () -> Unit,
) {
    val listState = rememberLazyListState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
    ) {
        AnimatedToolbar(
            R.string.title_activity_logs.localizable(),
            listState,
            null,
            trailingContent = {
                IconButton(
                    onClick = onOpenManualWorkout,
                    modifier = Modifier.testTag("add_manual_workout_button"),
                ) {
                    Text("+")
                }
            },
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                TodayStatsArea(
                    todayStatsUiState,
                    onHideTodayStats,
                    onRequestPermissions,
                    onOpenHealthConnect
                )
            }
            if (todayStatsUiState is Stats) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StepsCard(todayStatsUiState.steps)
                        DistanceCard(todayStatsUiState.steps)
                    }
                }
            }
            items(12) { month ->
                Month(month, uiState)
            }
        }
    }
    if (manualWorkoutUiState.sheetVisible) {
        var showDatePicker by remember { mutableStateOf(false) }
        val draft = manualWorkoutUiState.draft
        val translatedName = draft.type?.let { manualWorkoutTypeLabel(it) } ?: ""
        ModalBottomSheet(
            onDismissRequest = onDismissManualWorkout,
            modifier = Modifier.testTag("manual_workout_sheet"),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                Text(
                    text = R.string.manual_workout_title.localizable(),
                    style = Typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = R.string.manual_workout_type.localizable())
                ManualWorkoutType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectManualWorkoutType(type) }
                            .padding(vertical = 8.dp)
                            .testTag("manual_workout_type_${type.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = manualWorkoutTypeLabel(type),
                            color = if (draft.type == type) Primary else White,
                            modifier = Modifier.weight(1f),
                        )
                        if (draft.type == type) Text("✓", color = Primary)
                    }
                }
                manualWorkoutUiState.fieldErrors[ManualWorkoutField.TYPE]?.let {
                    ManualWorkoutErrorText(R.string.manual_workout_error_type_required)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = R.string.manual_workout_date.localizable(),
                    style = Typography.bodyMedium,
                )
                Text(
                    text = draft.date.toString(),
                    color = White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(vertical = 12.dp)
                        .testTag("manual_workout_date"),
                )
                manualWorkoutUiState.fieldErrors[ManualWorkoutField.DATE]?.let {
                    ManualWorkoutErrorText(R.string.manual_workout_error_date_out_of_range)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = R.string.manual_workout_type.localizable())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ManualWorkoutDurationField(
                        value = draft.hours,
                        label = R.string.manual_workout_duration_hours.localizable(),
                        testTag = "manual_workout_hours",
                        onValueChange = onUpdateManualWorkoutHours,
                    )
                    Text(":", style = Typography.titleLarge)
                    ManualWorkoutDurationField(
                        value = draft.minutes,
                        label = R.string.manual_workout_duration_minutes.localizable(),
                        testTag = "manual_workout_minutes",
                        onValueChange = onUpdateManualWorkoutMinutes,
                    )
                }
                manualWorkoutUiState.fieldErrors.values.distinct().forEach { error ->
                    ManualWorkoutErrorText(manualWorkoutErrorResource(error))
                }
                if (manualWorkoutUiState.persistenceError) {
                    ManualWorkoutErrorText(R.string.manual_workout_error_save_failed)
                }
                Button(
                    onClick = { onSubmitManualWorkout(translatedName) },
                    enabled = !manualWorkoutUiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_workout_save"),
                ) {
                    if (manualWorkoutUiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Text(R.string.manual_workout_save.localizable())
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        if (showDatePicker) {
            val today = LocalDate.now()
            val minimum = LocalDate.of(today.year, 1, 1)
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = manualWorkoutDateMillis(draft.date),
                selectableDates = object : androidx.compose.material3.SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = Instant.ofEpochMilli(utcTimeMillis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        return !date.isBefore(minimum) && !date.isAfter(today)
                    }
                },
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onUpdateManualWorkoutDate(
                                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate(),
                            )
                        }
                        showDatePicker = false
                    }) { Text(R.string.ok.localizable()) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(R.string.close.localizable())
                    }
                },
            ) {
                DatePicker(state = pickerState, showModeToggle = false)
            }
        }
    }
}

@Composable
private fun ManualWorkoutDurationField(
    value: String,
    label: String,
    testTag: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(2)) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.testTag(testTag).fillMaxWidth(0.42f),
    )
}

@Composable
private fun ManualWorkoutErrorText(@androidx.annotation.StringRes resourceId: Int) {
    Text(text = resourceId.localizable(), color = Primary, style = Typography.bodySmall)
}

private fun manualWorkoutErrorResource(error: ManualWorkoutValidationError): Int = when (error) {
    ManualWorkoutValidationError.TYPE_REQUIRED -> R.string.manual_workout_error_type_required
    ManualWorkoutValidationError.DATE_OUT_OF_RANGE -> R.string.manual_workout_error_date_out_of_range
    ManualWorkoutValidationError.HOURS_FORMAT -> R.string.manual_workout_error_hours_format
    ManualWorkoutValidationError.HOURS_OUT_OF_RANGE -> R.string.manual_workout_error_hours_out_of_range
    ManualWorkoutValidationError.MINUTES_FORMAT -> R.string.manual_workout_error_minutes_format
    ManualWorkoutValidationError.MINUTES_OUT_OF_RANGE -> R.string.manual_workout_error_minutes_out_of_range
    ManualWorkoutValidationError.DURATION_ZERO -> R.string.manual_workout_error_duration_zero
    ManualWorkoutValidationError.DURATION_TOO_LONG -> R.string.manual_workout_error_duration_too_long
}

@Composable
private fun manualWorkoutTypeLabel(type: ManualWorkoutType): String = when (type) {
    ManualWorkoutType.RUN -> R.string.manual_workout_type_run.localizable()
    ManualWorkoutType.STRENGTH_TRAINING -> R.string.manual_workout_type_strength_training.localizable()
    ManualWorkoutType.CYCLING -> R.string.manual_workout_type_cycling.localizable()
    ManualWorkoutType.SWIMMING -> R.string.manual_workout_type_swimming.localizable()
    ManualWorkoutType.WALKING -> R.string.manual_workout_type_walking.localizable()
    ManualWorkoutType.HIIT -> R.string.manual_workout_type_hiit.localizable()
}

private fun manualWorkoutDateMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

@Composable
private fun TodayStatsArea(
    state: TodayStatsUiState,
    onHideTodayStats: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenHealthConnect: () -> Unit,
) {
    when (state) {
        Hidden -> Unit
        Loading -> ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Gray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is Stats -> CaloriesCard(state.steps)
        InstallHealthConnect -> HealthConnectCard(
            message = R.string.health_connect_install_message.localizable(),
            onCardClick = onOpenHealthConnect,
            onHide = onHideTodayStats,
            tag = "today_stats_install_health_connect_card"
        )

        RequestHealthConnectPermissions -> HealthConnectCard(
            message = R.string.health_connect_permission_message.localizable(),
            onCardClick = onRequestPermissions,
            onHide = onHideTodayStats,
            tag = "today_stats_permissions_card"
        )
    }
}

@Composable
private fun HealthConnectCard(
    message: String,
    onCardClick: () -> Unit,
    onHide: () -> Unit,
    tag: String,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag(tag),
        colors = CardDefaults.elevatedCardColors(containerColor = Gray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = R.string.health_connect_title.localizable(),
                    style = Typography.headlineSmall,
                    color = Primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, style = Typography.bodyMedium, color = White)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onHide) {
                    Text(text = R.string.hide.localizable())
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onCardClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Black
                    )
                ) {
                    Text(text = R.string.ok.localizable())
                }
            }
        }
    }
}

@Composable
private fun StepsCard(steps: Long?, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .padding(bottom = 16.dp)
            .testTag("steps_card"),
        colors = CardDefaults.elevatedCardColors(containerColor = Gray)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
            Text(
                text = "${steps ?: 0}",
                style = Typography.headlineMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = R.string.steps_today.localizable(),
                style = Typography.headlineSmall,
                color = White
            )
        }
    }
}

@Composable
private fun DistanceCard(steps: Long?, modifier: Modifier = Modifier) {
    val distance = "%.2f".format(steps?.times(0.70)?.div(1000) ?: 0.0)
    ElevatedCard(
        modifier = modifier
            .testTag("distance_card"),
        colors = CardDefaults.elevatedCardColors(containerColor = Gray)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
            Text(
                text = R.string.steps_distance.localizable(distance),
                style = Typography.headlineMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = R.string.distance_today.localizable(),
                style = Typography.headlineSmall,
                color = White,
            )
        }
    }
}

@Composable
private fun CaloriesCard(steps: Long?, modifier: Modifier = Modifier) {
    val kCal = "%.2f".format(steps?.times(0.044) ?: 0.0)
    ElevatedCard(
        modifier = modifier
            .padding(bottom = 16.dp)
            .testTag("calories_card"),
        colors = CardDefaults.elevatedCardColors(containerColor = Gray)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
            Text(
                text = kCal,
                style = Typography.headlineMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = R.string.calories_burned.localizable(),
                style = Typography.headlineSmall,
                color = White,
            )
        }
    }
}

@Composable
private fun Month(
    month: Int,
    uiState: UiState.Loaded
) {
    val dateFormat = SimpleDateFormat("MMMM")
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, month)
    Text(
        text = dateFormat.format(calendar.time).capitalizeFirstLetter(),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
    FlowRow(
        modifier = Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (i in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            Day(calendar, i, uiState, month)
        }
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    )
}

@Composable
private fun Day(
    calendar: Calendar,
    index: Int,
    uiState: UiState.Loaded,
    month: Int
) {
    calendar.set(
        Calendar.DAY_OF_MONTH,
        index
    )
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val boxDate = calendar.time

    val item =
        uiState.logs[month]?.find { it.date.isSameDay(boxDate) }

    val modifier = Modifier
        .size(32.dp)
        .padding(4.dp)
        .clip(CircleShape)

    if (item == null) {
        Text(index.toString(), textAlign = TextAlign.Center, modifier = modifier.fillMaxSize())
    } else {
        Box(
            modifier = modifier
                .background(item.getStatus().getColor())
        ) {
//        Text(index.toString())
        }
    }
}

private fun Date.isSameDay(other: Date): Boolean {
    val cal1 = Calendar.getInstance()
    cal1.time = this
    val cal2 = Calendar.getInstance()
    cal2.time = other
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}


@Composable
@Preview
fun Preview() {
    val lol = UiState.Loaded(
        mapOf(1 to createSessions(10))
    )
    NorwegianTrainingTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Logs(innerPadding, lol,
                //Stats(7_452), {}, {}, {},
                RequestHealthConnectPermissions, ManualWorkoutUiState(), {}, {}, {}, {}, {}, {}, {}, {}, {}, {}

          )
        }
    }
}

private fun createSessions(sessionCount: Int): List<Session> {
    return buildList(capacity = sessionCount) {
        repeat(sessionCount) { index ->
            add(
                Session(
                    skipCount = Random.nextInt(0, 101),
                    date = Date()
                )
            )
        }
    }
}

private fun String.capitalizeFirstLetter() =
    if (this.isNotEmpty()) this[0].uppercase() + this.substring(1) else this
