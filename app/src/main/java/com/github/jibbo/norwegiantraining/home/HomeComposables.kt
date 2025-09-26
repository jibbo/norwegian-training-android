package com.github.jibbo.norwegiantraining.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.Toolbar
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.data.FakeWorkoutRepo
import com.github.jibbo.norwegiantraining.domain.GetAllWorkouts
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Typography

@Composable
internal fun HomeView(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(horizontal = 16.dp)
    ) {
        Header(viewModel)
        Streak(viewModel)
    }
}

@Composable
internal fun Header(viewModel: HomeViewModel) {
    val state = viewModel.uiStates.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.safeDrawingPadding()
    ) {
        Toolbar(
            name = R.string.welcome.localizable(state.value.username),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { viewModel.chartsClicked() }) {
            Icon(
                painter = painterResource(R.drawable.outline_area_chart_24),
                contentDescription = ""
            )
        }
        IconButton(onClick = { viewModel.settingsClicked() }) {
            Icon(
                painter = painterResource(R.drawable.baseline_settings_24),
                contentDescription = ""
            )
        }
    }
}

@Composable
internal fun Streak(viewModel: HomeViewModel) {
    val state = viewModel.uiStates.collectAsState()
    LazyColumn(
        contentPadding = PaddingValues(all = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val keys = state.value.workouts.keys.sorted()
        items(keys.size, { it }) { index ->
            val difficulty = keys.elementAt(index)
            Text(
                text = difficulty.name
            )
            val workouts = state.value.workouts[difficulty]?.sortedBy { it.id } ?: listOf()
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                workouts.forEach { workout ->
                    ElevatedCard {
                        Text(
                            text = workout.name,
                            modifier = Modifier.padding(6.dp),
                            style = Typography.titleMedium,
                        )
//                        Row {
//                            Text(
//                                text = workout.content,
//                                modifier = Modifier.padding(6.dp),
//                                style = Typography.bodyMedium,
//                            )
                        Text(
                            text = "${workout.totalTime}m",
                            modifier = Modifier.padding(6.dp),
                            style = Typography.bodyMedium,
                        )
//                        }
                        TextButton(onClick = {
                            viewModel.workoutClicked(workout.id)
                        }) {
                            Text(
                                text = R.string.start.localizable().uppercase(),
                            )
                        }

                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeViewPreview() {
    val settingsRepository = FakeSettingsRepository()
    val workoutRepository = FakeWorkoutRepo()
    NorwegianTrainingTheme {
        Scaffold { _ ->
            HomeView(
                HomeViewModel(
                    GetUsername(settingsRepository),
                    GetAllWorkouts(workoutRepository),
                    settingsRepository
                )
            )
        }
    }
}
