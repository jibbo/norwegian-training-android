package com.github.jibbo.norwegiantraining.home

import android.graphics.BlurMaskFilter
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.github.jibbo.norwegiantraining.domain.IsGracePeriodExpired
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
    LazyColumn(
        contentPadding = PaddingValues(all = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val keys = state.workouts.keys.sorted()
        items(keys.size, { it }) { index ->
            val difficulty = keys.elementAt(index)
            Text(
                text = difficulty.printableName().localizable(),
                modifier = Modifier.padding(bottom = 12.dp),
                style = Typography.bodyMedium,
            )
            val workouts = state.workouts[difficulty]?.sortedBy { it.id } ?: listOf()
            val scrollState = rememberScrollState()
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.horizontalScroll(scrollState)
            ) {
                workouts.forEach { workout ->
                    WorkoutCard(
                        workout,
                        state.recommendedWorkoutId,
                        state.recommendedLabel,
                        viewModel
                    )
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
    recommendedWorkoutId: Long?,
    @StringRes recommendedLabel: Int,
    viewModel: HomeViewModel,
) {
    val isRecommended = workout.id == recommendedWorkoutId
    val cardShape = RoundedCornerShape(12.dp)
    Box(
        modifier = if (isRecommended) {
            Modifier
                .graphicsLayer(clip = false)
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().also { p ->
                            p.asFrameworkPaint().apply {
                                isAntiAlias = true
                                color = Primary.copy(alpha = 0.6f).toArgb()
                                maskFilter = BlurMaskFilter(
                                    16.dp.toPx(),
                                    BlurMaskFilter.Blur.NORMAL
                                )
                            }
                        }
                        val cornerRadiusPx = 12.dp.toPx()
                        canvas.drawRoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            radiusX = cornerRadiusPx,
                            radiusY = cornerRadiusPx,
                            paint = paint
                        )
                    }
                }
        } else {
            Modifier
        }
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = Gray
            ),
            shape = cardShape,
            modifier = Modifier
                .width(200.dp)
                .then(
                    if (isRecommended) Modifier.border(1.5.dp, Primary, cardShape)
                    else Modifier
                ),
            onClick = {
                viewModel.workoutClicked(workout.id)
            }
        ) {
            if (isRecommended) {
                Text(
                    text = recommendedLabel.localizable(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = Typography.labelSmall,
                    color = Primary
                )
            }
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
}

@Preview
@Composable
fun HomeViewPreview() {
    val settingsRepository = FakeSettingsRepository()
    val workoutRepository = FakeWorkoutRepo()
    val analytics = FakeTracker()
    val isGracePeriod = IsGracePeriodExpired(settingsRepository)
    NorwegianTrainingTheme {
        Scaffold { innerPadding ->
            HomeView(
                HomeViewModel(
                    GetUsername(settingsRepository),
                    GetAllWorkouts(workoutRepository),
                    IsFreeTrial(settingsRepository),
                    IsOnboardingCompleted(settingsRepository),
                    GetRecommendedWorkoutId(settingsRepository),
                    analytics,
                    isGracePeriod
                ),
                innerPadding
            )
        }
    }
}
