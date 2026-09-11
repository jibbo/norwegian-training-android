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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutField
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomWorkoutActivity : BaseActivity() {
    private val viewModel: CustomWorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val workoutId = intent.getLongExtra(EXTRA_WORKOUT_ID, INVALID_WORKOUT_ID)
            .takeUnless { it == INVALID_WORKOUT_ID }

        setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                CustomWorkoutFormScreen(viewModel, workoutId, onBack = ::finish)
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
    onBack: () -> Unit,
) {
    LaunchedEffect(workoutId) {
        viewModel.initialize(workoutId)
    }
    val state by viewModel.uiState.collectAsState()
    val title = when (state.mode) {
        CustomWorkoutFormMode.CREATE -> R.string.custom_workout_create_title
        CustomWorkoutFormMode.EDIT -> R.string.custom_workout_edit_title
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
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
            )
        },
    ) { innerPadding ->
        CustomWorkoutFormShell(
            innerPadding = innerPadding,
            state = state,
            viewModel = viewModel,
            onSave = { viewModel.save() },
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
