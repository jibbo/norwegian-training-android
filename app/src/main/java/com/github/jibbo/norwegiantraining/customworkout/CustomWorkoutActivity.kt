package com.github.jibbo.norwegiantraining.customworkout

import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutField
import com.github.jibbo.norwegiantraining.service.WorkoutTimerState
import com.github.jibbo.norwegiantraining.service.WorkoutTimerStateManager
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class CustomWorkoutActivity : BaseActivity() {
    private val viewModel: CustomWorkoutViewModel by viewModels()
    @javax.inject.Inject
    lateinit var timerStateManager: WorkoutTimerStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val workoutId = intent.getLongExtra(EXTRA_WORKOUT_ID, INVALID_WORKOUT_ID)
            .takeUnless { it == INVALID_WORKOUT_ID }

        setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                CustomWorkoutFormScreen(
                    viewModel,
                    workoutId,
                    timerStateManager.state,
                    onBack = ::finish,
                )
            }
        }
    }

    companion object {
        const val EXTRA_WORKOUT_ID = "workout_id"
        private const val INVALID_WORKOUT_ID = Long.MIN_VALUE
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWorkoutFormScreen(
    viewModel: CustomWorkoutViewModel,
    workoutId: Long?,
    timerState: StateFlow<WorkoutTimerState>,
    onBack: () -> Unit,
) {
    LaunchedEffect(workoutId) {
        viewModel.initialize(workoutId)
    }
    val state by viewModel.uiState.collectAsState()
    val currentTimerState by timerState.collectAsState()
    val isWorkoutActive = currentTimerState.workoutId != -1L && !currentTimerState.isCompleted
    val deleteLabel = stringResource(R.string.custom_workout_delete)
    val fallbackName = stringResource(R.string.custom_workout_default_name)
    val title = when (state.mode) {
        CustomWorkoutFormMode.CREATE -> R.string.custom_workout_create_title
        CustomWorkoutFormMode.EDIT -> R.string.custom_workout_edit_title
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    if (state.persistenceError == CustomWorkoutPersistenceError.ACTIVE_WORKOUT) {
        AlertDialog(
            onDismissRequest = viewModel::clearPersistenceError,
            title = { Text(stringResource(R.string.custom_workout_active_title)) },
            text = { Text(stringResource(R.string.custom_workout_active_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::clearPersistenceError) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    if (state.deleteRequested) {
        AlertDialog(
            onDismissRequest = viewModel::clearDeleteRequest,
            title = { Text(stringResource(R.string.custom_workout_delete_title)) },
            text = { Text(stringResource(R.string.custom_workout_delete_message)) },
            dismissButton = {
                TextButton(onClick = viewModel::clearDeleteRequest) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(isWorkoutActive) }) {
                    Text(stringResource(R.string.delete))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title)) },
                navigationIcon = {
                    Button(onClick = onBack) {
                        Text(stringResource(R.string.back))
                    }
                },
                actions = {
                    if (state.mode == CustomWorkoutFormMode.EDIT) {
                        IconButton(
                            onClick = viewModel::requestDelete,
                            modifier = Modifier.semantics {
                                contentDescription = deleteLabel
                            },
                        ) {
                            Text(text = "🗑️")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        CustomWorkoutFormShell(
            innerPadding = innerPadding,
            state = state,
            viewModel = viewModel,
            onSave = { viewModel.save(isWorkoutActive, fallbackName) },
        )
    }
}

@Composable
private fun CustomWorkoutFormShell(
    innerPadding: PaddingValues,
    state: CustomWorkoutFormState,
    viewModel: CustomWorkoutViewModel,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.draft.name,
            onValueChange = viewModel::updateName,
            label = { Text(stringResource(R.string.custom_workout_name)) },
            isError = state.validationErrors.containsKey(CustomWorkoutField.NAME),
            modifier = Modifier.fillMaxWidth(),
        )
        CustomWorkoutIconPicker(
            selectedIcon = state.draft.icon,
            onIconSelected = viewModel::updateIcon,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DurationField(
                value = state.draft.workMinutes,
                label = R.string.custom_workout_work_minutes,
                isError = state.validationErrors.containsKey(CustomWorkoutField.WORK_MINUTES),
                onValueChange = viewModel::updateWorkMinutes,
            )
            DurationField(
                value = state.draft.workSeconds,
                label = R.string.custom_workout_work_seconds,
                isError = state.validationErrors.containsKey(CustomWorkoutField.WORK_SECONDS),
                onValueChange = viewModel::updateWorkSeconds,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DurationField(
                value = state.draft.restMinutes,
                label = R.string.custom_workout_rest_minutes,
                isError = state.validationErrors.containsKey(CustomWorkoutField.REST_MINUTES),
                onValueChange = viewModel::updateRestMinutes,
            )
            DurationField(
                value = state.draft.restSeconds,
                label = R.string.custom_workout_rest_seconds,
                isError = state.validationErrors.containsKey(CustomWorkoutField.REST_SECONDS),
                onValueChange = viewModel::updateRestSeconds,
            )
        }
        OutlinedTextField(
            value = state.draft.rounds,
            onValueChange = viewModel::updateRounds,
            label = { Text(stringResource(R.string.custom_workout_rounds)) },
            isError = state.validationErrors.containsKey(CustomWorkoutField.ROUNDS),
            modifier = Modifier.fillMaxWidth(),
        )
        state.persistenceError?.let {
            Text(stringResource(R.string.custom_workout_save_error))
        }
        Button(
            onClick = onSave,
            enabled = !state.isSaving && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save))
        }
    }
}

@Composable
private fun RowScope.DurationField(
    value: String,
    label: Int,
    isError: Boolean,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        isError = isError,
        modifier = Modifier.weight(1f),
    )
}
