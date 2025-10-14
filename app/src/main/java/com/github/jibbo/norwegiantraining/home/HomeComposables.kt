package com.github.jibbo.norwegiantraining.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.Toolbar
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.data.FakeWorkoutRepo
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.domain.GetAllWorkouts
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Typography

@Composable
internal fun HomeView(viewModel: HomeViewModel, innerPadding: PaddingValues) {
    val state = viewModel.uiStates.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = verticalGradient(
                    colors = listOf(
                        Color.DarkGray,
                        Black
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.runner_illustration),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(800.dp)
                .align(Alignment.BottomEnd)
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
    ) {
        if (state.value is UiState.Loading) {
            CircularProgressIndicator()
        } else {
            Header(viewModel)
            Box(
                modifier = Modifier.safeDrawingPadding(),
                contentAlignment = Alignment.Center
            ) {
                Workouts(viewModel)
            }
        }

    }
}

@Composable
internal fun Header(viewModel: HomeViewModel) {
    val state = viewModel.uiStates.collectAsState().value as UiState.Loaded
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .safeDrawingPadding()
    ) {
        Toolbar(
            name = R.string.welcome.localizable(state.username ?: ""),
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
internal fun Workouts(viewModel: HomeViewModel) {
    val state = viewModel.uiStates.collectAsState().value as UiState.Loaded
    LazyColumn(
        contentPadding = PaddingValues(all = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val keys = state.workouts.keys.sorted()
        items(keys.size, { it }) { index ->
            val difficulty = keys.elementAt(index)
            Text(
                text = difficulty.printableName().localizable(),
                modifier = Modifier.padding(bottom = 12.dp),
            )
            val workouts = state.workouts[difficulty]?.sortedBy { it.id } ?: listOf()
            val scrollState = rememberScrollState()
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.horizontalScroll(scrollState)
            ) {
                workouts.forEach { workout ->
                    WorkoutCard(workout, viewModel)
                }
            }
        }
    }
//    LazyVerticalGrid(
//        columns = GridCells.Adaptive(minSize = 150.dp),
//        verticalArrangement = Arrangement.spacedBy(6.dp),
//        horizontalArrangement = Arrangement.spacedBy(6.dp),
//        modifier = Modifier.padding(horizontal = 6.dp)
//    ) {
//        val workouts = state.value.workouts.flatMap { it.value }.sortedBy { it.id }
//        items(workouts.size, { it }) { index ->
//            WorkoutCard(workouts[index], viewModel)
//        }
//    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    viewModel: HomeViewModel
) {
    val splitSize = workout.splitText(
        withWarmup = false,
        withCooldown = false
    )
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Black
        ),
        modifier = Modifier
            .width(200.dp)
            .height(125.dp),
        onClick = {
            viewModel.workoutClicked(workout.id)
        }
    ) {
        Text(
            text = workout.name,
            modifier = Modifier.padding(8.dp),
            style = Typography.titleMedium,
        )
        Text(
            text = R.string.workout_time.localizable(workout.totalTime, workout.restTime()),
            modifier = Modifier.padding(8.dp),
            style = Typography.bodyMedium,
        )
        Text(
            text = R.string.workout_split.localizable(splitSize),
            modifier = Modifier.padding(8.dp),
            style = Typography.bodyMedium,
        )
//        TextButton(onClick = {
//            viewModel.workoutClicked(workout.id)
//        }, modifier = Modifier.align(Alignment.End)) {
//            Text(
//                text = R.string.start.localizable().uppercase(),
//            )
//        }
    }
}

@Preview
@Composable
fun HomeViewPreview() {
    val settingsRepository = FakeSettingsRepository()
    val workoutRepository = FakeWorkoutRepo()
    NorwegianTrainingTheme {
        Scaffold { innerPadding ->
            HomeView(
                HomeViewModel(
                    GetUsername(settingsRepository),
                    GetAllWorkouts(workoutRepository),
                    settingsRepository
                ),
                innerPadding
            )
        }
    }
}
