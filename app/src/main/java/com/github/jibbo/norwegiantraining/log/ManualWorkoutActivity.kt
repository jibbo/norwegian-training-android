@file:SuppressLint("NewApi")

package com.github.jibbo.norwegiantraining.log

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSessionRepo
import com.github.jibbo.norwegiantraining.data.FakeTracker
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutField
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutType
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutValidationError
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutViewModel
import com.github.jibbo.norwegiantraining.domain.SaveManualWorkoutUseCase
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.Gray
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@AndroidEntryPoint
class ManualWorkoutActivity : BaseActivity() {
    private val viewModel: ManualWorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialDate = intent.getStringExtra(EXTRA_INITIAL_DATE)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
        setContent {
            LaunchedEffect(Unit) {
                viewModel.savedSessionEvent.collect { session ->
                    setResult(
                        RESULT_OK, Intent().putExtra(
                            EXTRA_SAVED_DATE,
                            session.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                .toString()
                        )
                    )
                    finish()
                }
            }
            NorwegianTrainingTheme(darkTheme = true) {
                ManualWorkoutScreen(viewModel, initialDate, ::finish)
            }
        }
    }

    companion object {
        const val EXTRA_INITIAL_DATE = "initial_date"
        const val EXTRA_SAVED_DATE = "saved_date"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManualWorkoutScreen(
    viewModel: ManualWorkoutViewModel, initialDate: LocalDate, onBack: () -> Unit
) {
    LaunchedEffect(initialDate) { viewModel.open(initialDate) }
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val draft = state.draft
    val translatedName = draft.type?.let { manualWorkoutTypeLabel(it) } ?: ""
    Scaffold(topBar = {
        TopAppBar(title = { Text(R.string.manual_workout_title.localizable()) }, actions = {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back")) {
                Icon(
                    painter = painterResource(R.drawable.outline_close_24), contentDescription = ""
                )
            }
        })
    }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.weight(1f))
            Text(R.string.manual_workout_type.localizable())
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ManualWorkoutType.values().forEach { type ->
                    Button(
                        onClick = { viewModel.selectType(type) },
                        modifier = Modifier
                            .fillMaxWidth(0.48f)
                            .testTag("manual_workout_type_${type.name}"),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            if (draft.type == type) Primary else Gray,
                            if (draft.type == type) Black else White
                        ),
                    ) {
                        Text(
                            manualWorkoutTypeLabel(type), textAlign = TextAlign.Center, maxLines = 2
                        )
                    }
                }
            }
            state.fieldErrors[ManualWorkoutField.TYPE]?.let {
                ManualWorkoutErrorText(
                    R.string.manual_workout_error_type_required, "manual_workout_error_type"
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(R.string.manual_workout_date.localizable(), style = Typography.bodyMedium)
            TextButton(onClick = {showDatePicker = true}, modifier = Modifier.border(1.dp, White, shape = RoundedCornerShape(6.dp))) {
                Text(
                    draft.date.toString(),
                    color = White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("manual_workout_date"))
            }

            state.fieldErrors[ManualWorkoutField.DATE]?.let { ManualWorkoutErrorText(R.string.manual_workout_error_date_out_of_range) }
            Spacer(Modifier.height(16.dp))
            Text(R.string.manual_workout_duration.localizable())
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DurationField(
                    draft.hours,
                    R.string.manual_workout_duration_hours.localizable(),
                    "manual_workout_hours",
                    viewModel::updateHours
                )
                Text(":", style = Typography.titleLarge)
                DurationField(
                    draft.minutes,
                    R.string.manual_workout_duration_minutes.localizable(),
                    "manual_workout_minutes",
                    viewModel::updateMinutes
                )
            }
            state.fieldErrors.values.distinct()
                .forEach { ManualWorkoutErrorText(manualWorkoutErrorResource(it)) }
            if (state.persistenceError) ManualWorkoutErrorText(R.string.manual_workout_error_save_failed)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    viewModel.submit(translatedName)
                },
                enabled = !state.isSaving,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .testTag("manual_workout_save")
                    .imePadding()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp))
                } else {
                    val text =
                        R.string.manual_workout_save.localizable().toUpperCase(Locale.current)
                    Text(
                        text = text,
                        style = Typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
    if (showDatePicker) {
        val today = LocalDate.now()
        val minimum = LocalDate.of(today.year, 1, 1)
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = draft.date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    return !date.isBefore(minimum) && !date.isAfter(today)
                }
            },
        )
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let {
                    viewModel.updateDate(
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    )
                }; showDatePicker = false
            }) { Text(R.string.ok.localizable()) }
        }, dismissButton = {
            TextButton(onClick = {
                showDatePicker = false
            }) { Text(R.string.close.localizable()) }
        }) { DatePicker(state = pickerState, showModeToggle = false) }
    }
}

@Composable
private fun DurationField(
    value: String, label: String, tag: String, onValueChange: (String) -> Unit
) = OutlinedTextField(
    value,
    { onValueChange(it.filter(Char::isDigit).take(2)) },
    label = { Text(label) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    singleLine = true,
    modifier = Modifier
        .testTag(tag)
        .fillMaxWidth(0.3f)
)

@Composable
private fun ManualWorkoutErrorText(resource: Int, tag: String? = null) = Text(
    resource.localizable(),
    color = Primary,
    style = Typography.bodySmall,
    modifier = if (tag == null) Modifier else Modifier.testTag(tag)
)

private fun manualWorkoutErrorResource(error: ManualWorkoutValidationError) = when (error) {
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
private fun manualWorkoutTypeLabel(type: ManualWorkoutType) = when (type) {
    ManualWorkoutType.RUN -> R.string.manual_workout_type_run.localizable()
    ManualWorkoutType.STRENGTH_TRAINING -> R.string.manual_workout_type_strength_training.localizable()
    ManualWorkoutType.CYCLING -> R.string.manual_workout_type_cycling.localizable()
    ManualWorkoutType.SWIMMING -> R.string.manual_workout_type_swimming.localizable()
    ManualWorkoutType.WALKING -> R.string.manual_workout_type_walking.localizable()
    ManualWorkoutType.HIIT -> R.string.manual_workout_type_hiit.localizable()
}

@Preview
@Composable
private fun Preview() {
    NorwegianTrainingTheme {
        ManualWorkoutScreen(
            viewModel = ManualWorkoutViewModel(
                SaveManualWorkoutUseCase(
                    FakeSessionRepo(), FakeTracker()
                )
            ), initialDate = LocalDate.now(), onBack = {})
    }
}
