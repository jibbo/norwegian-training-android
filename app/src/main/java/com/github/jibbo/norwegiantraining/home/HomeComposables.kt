package com.github.jibbo.norwegiantraining.home

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
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
        WakeBackground()
    }

    if (isLandscape) {
        LandscapeLayout(viewModel, innerPadding)
    } else {
        PortraitLayout(viewModel, innerPadding)
    }
}

@Composable
private fun WakeBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "wake_transition")
    
    // Animation value from 0 to 1, looping continuously (slower animation)
    val animValue = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wake_animation"
    ).value
    
    val wakeLineCount = 12
    val numWakeLines = remember { wakeLineCount }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val canvasSize = size
                
                // Boat position: moves from bottom-left (0, height) to top-right (width, 0)
                val boatX = canvasSize.width * animValue
                val boatY = canvasSize.height * (1f - animValue)
                
                // Wake direction: -45 degrees goes from bottom-left to top-right
                // In Canvas coordinates: x increases (right), y decreases (up)
                val boatDirectionRadians = Math.toRadians(-45.0)
                
                // Calculate full diagonal length for corner-to-corner lines
                val diagonalLength = Math.sqrt(
                    (canvasSize.width.toDouble() * canvasSize.width + canvasSize.height.toDouble() * canvasSize.height)
                ).toFloat()
                
                // Draw wake lines extending from bottom-left to top-right
                for (i in 0 until numWakeLines) {
                    // Distribute lines in a V-shape pattern along the boat's path
                    val angleFraction = i.toFloat() / (numWakeLines - 1)
                    val angleDegrees = -30f + (angleFraction * 60f) // -30 to +30 degrees relative to path
                    val angleRadians = Math.toRadians(angleDegrees.toDouble())
                    
                    // Wake extends along boat's path: -45° (from bottom-left to top-right)
                    val wakeAngle = boatDirectionRadians + angleRadians
                    
                    // Line length varies based on animation progress (acceleration effect)
                    val lineProgress = animValue.toDouble().pow(1.5)
                    
                    // Calculate line start at boat position
                    val lineStartX = boatX
                    val lineStartY = boatY
                    
                    // Max line length: full diagonal (corner-to-corner) with extra margin
                    val maxLineLength = diagonalLength * 3.0f
                    val lineLength = lineProgress.toFloat() * maxLineLength
                    
                    // Draw sinusoidal line using multiple small segments
                    val numSegments = 20
                    val waveAmplitude = 30f // Increased amplitude to make sinusoidal effect obvious
                    val waveFrequency = 5f // 5 waves per line length
                    
                    for (j in 0..numSegments) {
                        val segmentFraction = j.toFloat() / numSegments
                        val segmentLength = segmentFraction * lineLength
                        
                        // Base position on straight line along boat's path
                        val baseX = lineStartX + segmentLength * cos(wakeAngle).toFloat()
                        val baseY = lineStartY + segmentLength * sin(wakeAngle).toFloat()
                        
                        // Sinusoidal offset perpendicular to line direction
                        val perpendicularAngle = wakeAngle + Math.PI / 2.0
                        val sinusoidalOffset = sin(segmentLength * waveFrequency * 2 * Math.PI / maxLineLength) * waveAmplitude
                        
                        val offsetX = (sinusoidalOffset * cos(perpendicularAngle)).toFloat()
                        val offsetY = (sinusoidalOffset * sin(perpendicularAngle)).toFloat()
                        
                        val segEndX = baseX + offsetX
                        val segEndY = baseY + offsetY
                        
                        // Previous segment point (or start point for first segment)
                        if (j > 0) {
                            val prevSegmentFraction = (j - 1).toFloat() / numSegments
                            val prevSegmentLength = prevSegmentFraction * lineLength
                            val prevBaseX = lineStartX + prevSegmentLength * cos(wakeAngle).toFloat()
                            val prevBaseY = lineStartY + prevSegmentLength * sin(wakeAngle).toFloat()
                            val prevSinusoidalOffset = sin(prevSegmentLength * waveFrequency * 2 * Math.PI / maxLineLength) * waveAmplitude
                            val prevOffsetX = (prevSinusoidalOffset * cos(perpendicularAngle)).toFloat()
                            val prevOffsetY = (prevSinusoidalOffset * sin(perpendicularAngle)).toFloat()
                            
                            drawLine(
                                color = Primary.copy(alpha = 0.8f),
                                start = Offset(prevBaseX + prevOffsetX, prevBaseY + prevOffsetY),
                                end = Offset(segEndX, segEndY),
                                strokeWidth = 2f + (angleFraction * 3f)
                            )
                        }
                    }
                }
            }
    )
}

@Composable
private fun PortraitLayout(
    viewModel: HomeViewModel,
    innerPadding: PaddingValues
) {
    val state = viewModel.uiStates.collectAsState().value
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
    ) {
        when (state) {
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Loaded -> {
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left column: Header + Streak + Next Up
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
    when (val state = viewModel.uiStates.collectAsState().value) {
        is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(12.dp))
        is UiState.Loaded -> {
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
    }
}

@Composable
private fun AllWorkouts(viewModel: HomeViewModel) {
    when (val state = viewModel.uiStates.collectAsState().value) {
        is UiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.padding(12.dp))
        }

        is UiState.Loaded -> {
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
    }
}

@Composable
private fun StreakWidget(viewModel: HomeViewModel) {
    when (val state = viewModel.uiStates.collectAsState().value) {
        is UiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.padding(12.dp))
        }

        is UiState.Loaded -> {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                onClick = { TODO() }
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
    when (val state = viewModel.uiStates.collectAsState().value) {
        is UiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.safeDrawingPadding())
        }

        is UiState.Loaded -> {
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
    }
}

@Composable
internal fun Workouts(viewModel: HomeViewModel) {
    when (val state = viewModel.uiStates.collectAsState().value) {
        is UiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.padding(12.dp))
        }

        is UiState.Loaded -> {
            val allWorkouts = state.workouts.values.flatten().sortedBy { it.id }
            val recommendedWorkout = allWorkouts.find { it.id == state.recommendedWorkoutId }
            val otherWorkouts = allWorkouts.filter { it.id != state.recommendedWorkoutId }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = R.string.home_next_up.localizable(),
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
            containerColor = Color.Black
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
            modifier = Modifier.padding(4.dp),
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
