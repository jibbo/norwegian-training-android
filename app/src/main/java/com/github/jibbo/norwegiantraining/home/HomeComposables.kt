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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.Toolbar
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSessionRepo
import com.github.jibbo.norwegiantraining.data.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.data.FakeTracker
import com.github.jibbo.norwegiantraining.data.FakeWorkoutRepo
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.domain.GetAllWorkouts
import com.github.jibbo.norwegiantraining.domain.GetRecommendedWorkoutId
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.domain.GetWeeklySessionsUseCase
import com.github.jibbo.norwegiantraining.domain.IsFreeTrial
import com.github.jibbo.norwegiantraining.domain.IsOnboardingCompleted
import com.github.jibbo.norwegiantraining.log.getColor
import com.github.jibbo.norwegiantraining.log.getStatus
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.DarkPrimary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White
import java.util.Calendar

@Composable
internal fun HomeView(viewModel: HomeViewModel, innerPadding: PaddingValues) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = verticalGradient(
                    colors = listOf(
                        DarkPrimary,
                        Black
                    )
                )
            )
    ) {
    }

    if (isLandscape) {
        LandscapeLayout(viewModel, innerPadding)
    } else {
        PortraitLayout(viewModel, innerPadding)
    }
}

@Composable
private fun PortraitLayout(
    viewModel: HomeViewModel,
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        CircularProgressIndicator()
        Header(viewModel)
        StreakWidget(viewModel)
        Box(
            modifier = Modifier.safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            Workouts(viewModel)
        }
    }
}

@Composable
private fun LandscapeLayout(
    viewModel: HomeViewModel,
    innerPadding: PaddingValues
) {
    Row(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left column: Header + Streak + Next Up
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(viewModel)
            StreakWidget(viewModel)
            NextUpWorkout(viewModel)
        }

        // Right column: All Workouts (scrollable LazyColumn)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            AllWorkouts(viewModel)
        }
    }
}

@Composable
private fun NextUpWorkout(viewModel: HomeViewModel) {
    val state = viewModel.uiStates.collectAsState().value as UiState.Loaded
    val allWorkouts = state.workouts.values.flatten().sortedBy { it.id }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(12.dp)
    ) {
        Text(
            text = R.string.home_next_up.localizable(),
            modifier = Modifier.padding(bottom = 12.dp),
            style = Typography.titleLarge,
            fontWeight = FontWeight.Normal
        )
        val workout = allWorkouts.firstOrNull() ?: return
        WorkoutCard(
            workout,
            viewModel
        )
    }
}

@Composable
private fun AllWorkouts(viewModel: HomeViewModel) {
    val state = viewModel.uiStates.collectAsState().value as UiState.Loaded
    val allWorkouts = state.workouts.values.flatten().sortedBy { it.id }
    val otherWorkouts = allWorkouts.filter { it.id != state.recommendedWorkoutId }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = R.string.home_all_workouts.localizable(),
                modifier = Modifier.padding(bottom = 12.dp),
                style = Typography.titleLarge,
                fontWeight = FontWeight.Normal
            )
        }
        items(otherWorkouts.size, { it }) { index ->
            WorkoutCard(otherWorkouts[index], viewModel)
        }
    }
}

@Composable
private fun StreakWidget(viewModel: HomeViewModel) {
    val state = viewModel.uiStates.collectAsState().value as UiState.Loaded
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Black
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        onClick = { TODO() }
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(color = Primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "",
                        tint = Black,
                    )
                }
            }
            Column {
                Month(state.weeklySessions)
            }
        }
    }
}

@Composable
private fun Month(weeklySessions: List<Session?>) {
    val locale = LocalLocale.current.platformLocale
    val calendar = Calendar.getInstance(locale)
    val firstDayOfWeek = calendar.firstDayOfWeek

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val daysInWeek = 7
        items(daysInWeek) { index ->
            val dayOfWeek = (firstDayOfWeek + index - 1) % 7 + 1
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
            val dayName = calendar.getDisplayName(
                Calendar.DAY_OF_WEEK,
                Calendar.SHORT,
                locale
            ).orEmpty()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = weeklySessions[index]?.getStatus()
                                ?.getColor() ?: White,
                            shape = CircleShape
                        )
                ) {}
                Text(
                    text = dayName,
                    style = Typography.labelSmall,
                    color = White,
                    modifier = Modifier.padding(top = 6.dp)
                )
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
        } else if (allWorkouts.isNotEmpty()) {
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
            containerColor = Black
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
            text = R.string.workout_time.localizable(workout.totalTime),
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
