package com.github.jibbo.norwegiantraining.customworkout

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutField
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWorkoutFormScreen(
    viewModel: CustomWorkoutViewModel,
    workoutId: Long?,
    onBack: () -> Unit,
) {
    LaunchedEffect(workoutId) {
        viewModel.initialize(workoutId)
    }
    val state by viewModel.uiState.collectAsState()
    val currentTimerState by viewModel.timerState.collectAsState()
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
                actions = {
                    if (state.mode == CustomWorkoutFormMode.EDIT) {
                        IconButton(
                            onClick = viewModel::requestDelete,
                            modifier = Modifier.semantics {
                                contentDescription = deleteLabel
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_delete_outline_24),
                                contentDescription = ""
                            )
                        }
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.outline_close_24),
                                contentDescription = ""
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        CustomWorkoutFormShell(
            innerPadding = innerPadding,
            state = state,
            onNameChange = viewModel::updateName,
            onIconSelected = viewModel::updateIcon,
            onWorkMinutesChange = viewModel::updateWorkMinutes,
            onWorkSecondsChange = viewModel::updateWorkSeconds,
            onRestMinutesChange = viewModel::updateRestMinutes,
            onRestSecondsChange = viewModel::updateRestSeconds,
            onRoundsChange = viewModel::updateRounds,
            onSave = { viewModel.save(isWorkoutActive, fallbackName) },
        )
    }
}

@Composable
private fun CustomWorkoutFormShell(
    innerPadding: PaddingValues,
    state: CustomWorkoutFormState,
    onNameChange: (String) -> Unit,
    onIconSelected: (String?) -> Unit,
    onWorkMinutesChange: (String) -> Unit,
    onWorkSecondsChange: (String) -> Unit,
    onRestMinutesChange: (String) -> Unit,
    onRestSecondsChange: (String) -> Unit,
    onRoundsChange: (String) -> Unit,
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
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.custom_workout_name)) },
            isError = state.validationErrors.containsKey(CustomWorkoutField.NAME),
            modifier = Modifier.fillMaxWidth(),
        )
        CustomWorkoutIconPicker(
            selectedIcon = state.draft.icon,
            onIconSelected = onIconSelected,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DurationField(
                value = state.draft.workMinutes,
                label = R.string.custom_workout_work_minutes,
                isError = state.validationErrors.containsKey(CustomWorkoutField.WORK_MINUTES),
                onValueChange = onWorkMinutesChange,
            )
            DurationField(
                value = state.draft.workSeconds,
                label = R.string.custom_workout_work_seconds,
                isError = state.validationErrors.containsKey(CustomWorkoutField.WORK_SECONDS),
                onValueChange = onWorkSecondsChange,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DurationField(
                value = state.draft.restMinutes,
                label = R.string.custom_workout_rest_minutes,
                isError = state.validationErrors.containsKey(CustomWorkoutField.REST_MINUTES),
                onValueChange = onRestMinutesChange,
            )
            DurationField(
                value = state.draft.restSeconds,
                label = R.string.custom_workout_rest_seconds,
                isError = state.validationErrors.containsKey(CustomWorkoutField.REST_SECONDS),
                onValueChange = onRestSecondsChange,
            )
        }
        OutlinedTextField(
            value = state.draft.rounds,
            onValueChange = onRoundsChange,
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

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomWorkoutFormPreview() {
    NorwegianTrainingTheme(darkTheme = true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.custom_workout_create_title)) },
                    actions = {
                        IconButton(
                            onClick = { },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_delete_outline_24),
                                contentDescription = ""
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_close_24),
                                contentDescription = ""
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            CustomWorkoutFormShell(
                innerPadding = innerPadding,
                state = CustomWorkoutFormState(),
                onNameChange = {},
                onIconSelected = {},
                onWorkMinutesChange = {},
                onWorkSecondsChange = {},
                onRestMinutesChange = {},
                onRestSecondsChange = {},
                onRoundsChange = {},
                onSave = {},
            )
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
