package com.github.jibbo.norwegiantraining.customworkout

import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
        CustomWorkoutFormShell(innerPadding)
    }
}

@Composable
private fun CustomWorkoutFormShell(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.custom_workout_form_placeholder))
    }
}
