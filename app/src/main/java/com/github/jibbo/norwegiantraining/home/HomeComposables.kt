package com.github.jibbo.norwegiantraining.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.Toolbar
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.data.FakeTracker
import com.github.jibbo.norwegiantraining.data.FakeWorkoutRepo
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.domain.GetAllWorkouts
import com.github.jibbo.norwegiantraining.domain.GetRecommendedWorkoutId
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.domain.IsFreeTrial
import com.github.jibbo.norwegiantraining.domain.IsOnboardingCompleted
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.Gray
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White

@Composable
internal fun HomeView(viewModel: HomeViewModel, innerPadding: PaddingValues) {
    val state = viewModel.uiStates.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Black)
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
            modifier = Modifier.weight(1f),
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
    val allWorkouts = state.workouts.values.flatten().sortedBy { it.id }
    val recommendedWorkout = allWorkouts.find { it.id == state.recommendedWorkoutId }
    val otherWorkouts = allWorkouts.filter { it.id != state.recommendedWorkoutId }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(12.dp)
    ) {
        Text(
            text = R.string.home_next_up.localizable(),
            modifier = Modifier.padding(bottom = 12.dp),
            style = Typography.titleLarge,
            fontWeight = FontWeight.Normal
        )
        if (recommendedWorkout != null) {
            WorkoutCard(
                recommendedWorkout,
                viewModel
            )
        } else{
            WorkoutCard(
                allWorkouts[0],
                viewModel
            )
        }
        Text(
            text = R.string.home_all_workouts.localizable(),
            modifier = Modifier.padding(bottom = 12.dp),
            style = Typography.titleLarge,
            fontWeight = FontWeight.Normal
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(otherWorkouts.size, { it }) { index ->
                WorkoutCard(otherWorkouts[index], viewModel)
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    viewModel: HomeViewModel,
) {
    val cardShape = RoundedCornerShape(12.dp)
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Gray
        ),
        shape = cardShape,
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            viewModel.workoutClicked(workout.id)
        }
    ) {
        Text(
            text = workout.name,
            modifier = Modifier.padding(8.dp),
            style = Typography.titleMedium,
            color = White
        )
        Text(
            text = R.string.workout_time.localizable(workout.totalTime, workout.restTime()),
            modifier = Modifier.padding(8.dp),
            style = Typography.bodyMedium,
            color = White
        )
        Text(
            text = R.string.workout_kCal.localizable(workout.kCal),
            modifier = Modifier
                .padding(8.dp)
                .alpha(0.8f),
            style = Typography.bodySmall,
            color = White
        )
    }
}

@Preview
@Composable
fun HomeViewPreview() {
    val settingsRepository = FakeSettingsRepository()
    val workoutRepository = FakeWorkoutRepo()
    val analytics = FakeTracker()
    NorwegianTrainingTheme {
        Scaffold { innerPadding ->
            HomeView(
                HomeViewModel(
                    GetUsername(settingsRepository),
                    GetAllWorkouts(workoutRepository),
                    IsFreeTrial(settingsRepository),
                    IsOnboardingCompleted(settingsRepository),
                    GetRecommendedWorkoutId(settingsRepository),
                    analytics
                ),
                innerPadding
            )
        }
    }
}
