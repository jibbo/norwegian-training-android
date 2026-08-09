package com.github.jibbo.norwegiantraining.home

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.data.FakeRepos.FakeSessionRepo
import com.github.jibbo.norwegiantraining.data.FakeRepos.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.data.FakeRepos.FakeTracker
import com.github.jibbo.norwegiantraining.data.FakeRepos.FakeWorkoutRepo
import com.github.jibbo.norwegiantraining.data.FakeRepos.GetRecommendedWorkoutId
import com.github.jibbo.norwegiantraining.data.FakeRepos.GetUsername
import com.github.jibbo.norwegiantraining.data.FakeRepos.GetWeeklySessionsUseCase
import com.github.jibbo.norwegiantraining.data.FakeRepos.GetAllWorkouts
import com.github.jibbo.norwegiantraining.data.FakeRepos.IsFreeTrial
import com.github.jibbo.norwegiantraining.data.FakeRepos.IsOnboardingCompleted
import com.github.jibbo.norwegiantraining.data.Tracker
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.White
import com.github.jibbo.norwegiantraining.util.Localizable
import com.github.jibbo.norwegiantraining.util.Localizable
import com.github.jibbo.norwegiantraining.util.Localizable.localizable
import com.github.jibbo.norwegiantraining.util.coalesce
import java.util.Locale

@Composable
fun HomeView(viewModel: HomeViewModel, innerPadding: PaddingValues) {
    val uiState by viewModel.uiStates.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateProgress()
            delay(1000)
        }
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    when (uiState) {
        is UiState.Error -> Text(
            text = R.string.home_error.localizable(),
            modifier = Modifier.padding(16.dp),
            color = Color.Red
        )
        is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(12.dp))
        is UiState.Loaded -> {
            val state = (uiState as UiState.Loaded).state

            if (isLandscape) {
                LandscapeHomeView(state, viewModel)
            } else {
                PortraitHomeView(state, viewModel)
            }
        }
    }
}

@Composable
private fun PortraitHomeView(state: HomeState, viewModel: HomeViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = R.string.home_greeting.localizable(state.username),
                    style = NorwegianTrainingTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                IconButton(onClick = { viewModel.settingsClicked() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = R.string.settings.localizable(),
                        tint = White
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.size(16.dp)) }

        // Progress
        item {
            Text(
                text = R.string.home_progress.localizable(
                    state.currentWeekCompleted,
                    state.currentWeekGoal
                ),
                style = NorwegianTrainingTheme.typography.bodyMedium,
                color = White
            )
        }
        item { Spacer(modifier = Modifier.size(16.dp)) }

        item {
            val progress = state.currentWeekCompleted.toFloat() / state.currentWeekGoal.toFloat()
            androidx.compose.material3.LinearProgressIndicator(
                progress = coalesce(progress),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeWidth = 8.dp
            )
        }
        item { Spacer(modifier = Modifier.size(8.dp)) }

        item {
            Text(
                text = R.string.home_weeks.localizable(state.currentWeek),
                style = NorwegianTrainingTheme.typography.bodySmall,
                color = White.copy(alpha = 0.7f)
            )
        }
        item { Spacer(modifier = Modifier.size(32.dp)) }

        // Today's workout
        item {
            Text(
                text = R.string.home_today.localizable(),
                style = NorwegianTrainingTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        item { Spacer(modifier = Modifier.size(12.dp)) }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (!state.isFreeTrial) {
                            viewModel.paywallClicked()
                    } else {
                        viewModel.todayWorkoutClicked()
                    }
                }
            }
            ) {
                Text(
                    text = state.todayWorkout?.name ?: R.string.workout_not_available.localizable(),
                    modifier = Modifier.padding(8.dp),
                    style = NorwegianTrainingTheme.typography.titleMedium,
                    color = White
                )
                if (state.todayWorkout != null) {
                    Text(
                        text = R.string.workout_time.localizable(state.todayWorkout.totalTime),
                        modifier = Modifier.padding(4.dp),
                        style = NorwegianTrainingTheme.typography.bodyMedium,
                        color = White
                    )
                    Text(
                        text = R.string.workout_kCal.localizable(state.todayWorkout.kCal),
                        modifier = Modifier
                            .padding(8.dp)
                            .alpha(0.8f),
                        style = NorwegianTrainingTheme.typography.bodySmall,
                        color = White
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.size(32.dp)) }

        // Recent workouts
        item {
            Text(
                text = R.string.home_recent.localizable(),
                style = NorwegianTrainingTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        item { Spacer(modifier = Modifier.size(12.dp)) }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.recentWorkouts) { workout ->
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(150.dp),
                        onClick = {
                            if (!state.isFreeTrial) {
                                viewModel.paywallClicked()
                            } else {
                                viewModel.workoutClicked(workout.id)
                            }
                        }
                    ) {
                        Text(
                            text = workout.name,
                            modifier = Modifier.padding(8.dp),
                            style = NorwegianTrainingTheme.typography.titleMedium,
                            color = White
                        )
                        Text(
                            text = R.string.workout_time.localizable(workout.totalTime),
                            modifier = Modifier.padding(4.dp),
                            style = NorwegianTrainingTheme.typography.bodyMedium,
                            color = White
                        )
                        Text(
                            text = R.string.workout_kCal.localizable(workout.kCal),
                            modifier = Modifier
                                .padding(8.dp)
                                .alpha(0.8f),
                            style = NorwegianTrainingTheme.typography.bodySmall,
                            color = White
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.size(32.dp)) }

        // All workouts
        item {
            Text(
                text = R.string.home_all_workouts.localizable(),
                style = NorwegianTrainingTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        item { Spacer(modifier = Modifier.size(12.dp)) }

        // Grid of other workouts - laid out as rows of 2
        item {
            val otherWorkouts = state.otherWorkouts
            if (otherWorkouts.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (i in otherWorkouts.indices step 2) {
                        val pair = listOf(
                            otherWorkouts[i],
                            otherWorkouts.getOrNull(i + 1)
                        ).filterNotNull()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { workout ->
                                WorkoutCard(
                                    workout,
                                    viewModel,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(2 - pair.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandscapeHomeView(state: HomeState, viewModel: HomeViewModel) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        // Left column - header, progress, today
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = R.string.home_greeting.localizable(state.username),
                    style = NorwegianTrainingTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                IconButton(onClick = { viewModel.settingsClicked() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = R.string.settings.localizable(),
                        tint = White
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = R.string.home_progress.localizable(
                    state.currentWeekCompleted,
                    state.currentWeekGoal
                ),
                style = NorwegianTrainingTheme.typography.bodyMedium,
                color = White
            )
            Spacer(modifier = Modifier.size(16.dp))

            val progress = state.currentWeekCompleted.toFloat() / state.currentWeekGoal.toFloat()
            androidx.compose.material3.LinearProgressIndicator(
                progress = coalesce(progress),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeWidth = 8.dp
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = R.string.home_weeks.localizable(state.currentWeek),
                style = NorwegianTrainingTheme.typography.bodySmall,
                color = White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.size(32.dp))

            Text(
                text = R.string.home_today.localizable(),
                style = NorwegianTrainingTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.size(12.dp))

            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (!state.isFreeTrial) {
                        viewModel.paywallClicked()
                    } else {
                        viewModel.todayWorkoutClicked()
                    }
                }
            ) {
                Text(
                    text = state.todayWorkout?.name ?: R.string.workout_not_available.localizable(),
                    modifier = Modifier.padding(8.dp),
                    style = NorwegianTrainingTheme.typography.titleMedium,
                    color = White
                )
                if (state.todayWorkout != null) {
                    Text(
                        text = R.string.workout_time.localizable(state.todayWorkout.totalTime),
                        modifier = Modifier.padding(4.dp),
                        style = NorwegianTrainingTheme.typography.bodyMedium,
                        color = White
                    )
                    Text(
                        text = R.string.workout_kCal.localizable(state.todayWorkout.kCal),
                        modifier = Modifier
                            .padding(8.dp)
                            .alpha(0.8f),
                        style = NorwegianTrainingTheme.typography.bodySmall,
                        color = White
                    )
                }
            }
            Spacer(modifier = Modifier.size(32.dp))

            Text(
                text = R.string.home_recent.localizable(),
                style = NorwegianTrainingTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.size(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.recentWorkouts) { workout ->
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(150.dp),
                        onClick = {
                            if (!state.isFreeTrial) {
                                viewModel.paywallClicked()
                            } else {
                                viewModel.workoutClicked(workout.id)
                            }
                        }
                    ) {
                        Text(
                            text = workout.name,
                            modifier = Modifier.padding(8.dp),
                            style = NorwegianTrainingTheme.typography.titleMedium,
                            color = White
                        )
                        Text(
                            text = R.string.workout_time.localizable(workout.totalTime),
                            modifier = Modifier.padding(4.dp),
                            style = NorwegianTrainingTheme.typography.bodyMedium,
                            color = White
                        )
                        Text(
                            text = R.string.workout_kCal.localizable(workout.kCal),
                            modifier = Modifier
                                .padding(8.dp)
                                .alpha(0.8f),
                            style = NorwegianTrainingTheme.typography.bodySmall,
                            color = White
                        )
                    }
                }
            }
        }

        // Right column - all workouts grid
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = R.string.home_all_workouts.localizable(),
                style = NorwegianTrainingTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.size(12.dp))

            val otherWorkouts = state.otherWorkouts
            if (otherWorkouts.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (i in otherWorkouts.indices step 2) {
                        val pair = listOf(
                            otherWorkouts[i],
                            otherWorkouts.getOrNull(i + 1)
                        ).filterNotNull()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { workout ->
                                WorkoutCard(
                                    workout,
                                    viewModel,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(2 - pair.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(12.dp)
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.Black
        ),
        shape = cardShape,
        modifier = modifier.fillMaxWidth(),
        onClick = {
            viewModel.workoutClicked(workout.id)
        }
    ) {
        Text(
            text = workout.name,
            modifier = Modifier.padding(8.dp),
            style = NorwegianTrainingTheme.typography.titleMedium,
            color = White
        )
        Text(
            text = R.string.workout_time.localizable(workout.totalTime),
            modifier = Modifier.padding(4.dp),
            style = NorwegianTrainingTheme.typography.bodyMedium,
            color = White
        )
        Text(
            text = R.string.workout_kCal.localizable(workout.kCal),
            modifier = Modifier
                .padding(8.dp)
                .alpha(0.8f),
            style = NorwegianTrainingTheme.typography.bodySmall,
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
    val sessionRepository = FakeSessionRepo()
    val getWeeklySessions = GetWeeklySessionsUseCase(sessionRepository)
    NorwegianTrainingTheme {
        Scaffold { innerPadding ->
            HomeView(
                HomeViewModel(
                    GetUsername(settingsRepository),
                    GetAllWorkouts(workoutRepository),
                    IsFreeTrial(settingsRepository),
                    IsOnboardingCompleted(settingsRepository),
                    GetRecommendedWorkoutId(settingsRepository),
                    getWeeklySessions,
                    analytics
                ),
                innerPadding
            )
        }
    }
}
