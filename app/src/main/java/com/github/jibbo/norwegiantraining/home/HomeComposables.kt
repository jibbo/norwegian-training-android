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
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.AnimatedToolbar
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
import com.github.jibbo.norwegiantraining.domain.displayLabel
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
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

internal object WorkoutTransitionBounds {
    private val bounds = mutableMapOf<Long, androidx.compose.ui.geometry.Rect>()

    fun update(id: Long, rect: androidx.compose.ui.geometry.Rect) { bounds[id] = rect }
    fun get(id: Long) = bounds[id]
}

@Composable
internal fun HomeView(viewModel: HomeViewModel, innerPadding: PaddingValues) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val transitionOverlay by WorkoutTransitionState.overlay.collectAsState()

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

    TransitionOverlay(transitionOverlay)
}

@Composable
private fun TransitionOverlay(overlay: WorkoutTransitionState.OverlayState?) {
    val targetAlpha = if (overlay == null) 0f else 1f
    val alpha = androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = WorkoutTransitionState.TRANSITION_DURATION_MS.toInt(),
            easing = androidx.compose.animation.core.FastOutSlowInEasing,
        ),
        label = "homeTransitionOverlay",
    ).value
    if (alpha <= 0f) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawTransitionDim(alpha, overlay?.bounds)
            }
    )
}

private fun DrawScope.drawTransitionDim(alpha: Float, bounds: Rect?) {
    val highlight = bounds?.takeIf { it.width > 0f && it.height > 0f }
    if (highlight == null) {
        drawRect(Color.Black.copy(alpha = 0.5f * alpha))
        return
    }

    val baseDim = 0.32f * alpha
    val extraDim = 0.18f * alpha
    drawRect(Color.Black.copy(alpha = baseDim))
    drawRect(Color.Black.copy(alpha = extraDim), size = androidx.compose.ui.geometry.Size(size.width, highlight.top.coerceAtLeast(0f)))
    drawRect(
        Color.Black.copy(alpha = extraDim),
        topLeft = Offset(0f, highlight.bottom.coerceAtMost(size.height)),
        size = androidx.compose.ui.geometry.Size(size.width, (size.height - highlight.bottom).coerceAtLeast(0f)),
    )
    drawRect(
        Color.Black.copy(alpha = extraDim),
        topLeft = Offset(0f, highlight.top.coerceAtLeast(0f)),
        size = androidx.compose.ui.geometry.Size(highlight.left.coerceAtLeast(0f), highlight.height),
    )
    drawRect(
        Color.Black.copy(alpha = extraDim),
        topLeft = Offset(highlight.right.coerceAtMost(size.width), highlight.top.coerceAtLeast(0f)),
        size = androidx.compose.ui.geometry.Size((size.width - highlight.right).coerceAtLeast(0f), highlight.height),
    )
}

@Composable
private fun WakeBackground() {
    var animValue by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            animValue += 0.005f
            if (animValue >= 1f) animValue = 0f
            delay(16)
        }
    }

    val wakeLineCount = 12
    val numWakeLines = remember { wakeLineCount }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val canvasSize = size

                // Wake direction: -45 degrees goes from bottom-left to top-right
                val boatDirectionRadians = Math.toRadians(-45.0)

                // Calculate full diagonal length for corner-to-corner lines
                val diagonalLength = Math.sqrt(
                    (canvasSize.width.toDouble() * canvasSize.width + canvasSize.height.toDouble() * canvasSize.height)
                ).toFloat()

                // Max line length: slightly larger than diagonal to ensure it covers the screen
                val maxLineLength = diagonalLength * 1.2f

                // Draw wake lines from fixed bottom-left to top-right
                for (i in 0 until numWakeLines) {
                    // Distribute lines in a V-shape pattern around the -45° path
                    val angleFraction = i.toFloat() / (numWakeLines - 1)
                    val angleDegrees = -30f + (angleFraction * 60f) // -30 to +30 degrees relative to path
                    val angleRadians = Math.toRadians(angleDegrees.toDouble())

                    // Wake extends along boat's path: -45° (from bottom-left to top-right)
                    val wakeAngle = boatDirectionRadians + angleRadians

                    // Fixed starting point: bottom-left corner
                    val lineStartX = -50f
                    val lineStartY = canvasSize.height+50f

                    // Fixed line length (does not move)
                    val lineLength = maxLineLength

                    // Draw sinusoidal line using multiple small segments
                    val numSegments = 40
                    val waveAmplitude = 20f // Reduced for gentler waves
                    val waveFrequency = 1f // Reduced for fewer, wider waves

                    for (j in 0..numSegments) {
                        val segmentFraction = j.toFloat() / numSegments
                        val segmentLength = segmentFraction * lineLength

                        // Base position on straight line from bottom-left to top-right
                        val baseX = lineStartX + segmentLength * cos(wakeAngle).toFloat()
                        val baseY = lineStartY + segmentLength * sin(wakeAngle).toFloat()

                        // Sinusoidal offset perpendicular to line direction
                        val perpendicularAngle = wakeAngle + Math.PI / 2.0

                        // Animate the wave phase along the line.
                        // animValue 0→1 makes the wave travel from the line start to end,
                        // creating a floating/rippling effect along the fixed line.
                        val wavePhase = (segmentFraction - animValue) * waveFrequency * 2 * Math.PI
                        val sinusoidalOffset = sin(wavePhase) * waveAmplitude

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

                            val prevWavePhase = (prevSegmentFraction - animValue) * waveFrequency * 2 * Math.PI
                            val prevSinusoidalOffset = sin(prevWavePhase) * waveAmplitude
                            val prevOffsetX = (prevSinusoidalOffset * cos(perpendicularAngle)).toFloat()
                            val prevOffsetY = (prevSinusoidalOffset * sin(perpendicularAngle)).toFloat()

                            drawLine(
                                color = Primary.copy(alpha = 0.09f),
                                start = Offset(prevBaseX + prevOffsetX, prevBaseY + prevOffsetY),
                                end = Offset(segEndX, segEndY),
                                strokeWidth = 2.6f + (angleFraction * 3f)
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
    when (val state = viewModel.uiStates.collectAsState().value) {
        is UiState.Loading -> Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        is UiState.Loaded -> {
            val projection = state.workoutProjection

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Fixed header above the scrollable area
                Header(viewModel)

                // Scrollable content: streak, workouts
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    // Streak widget spanning full width
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        StreakWidget(viewModel)
                    }

                    // Your workouts section spanning full width
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = R.string.home_next_up.localizable(),
                                style = Typography.titleLarge,
                                fontWeight = FontWeight.Normal,
                            )
                            Text(
                                text = "+",
                                style = Typography.headlineMedium,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                    }

                    items(projection.yourWorkouts.size, { projection.yourWorkouts[it].id }) { index ->
                        WorkoutCard(projection.yourWorkouts[index], viewModel)
                    }

                    // All built-in workouts section spanning full width
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = R.string.home_all_workouts.localizable(),
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }

                    items(projection.remainingBuiltIns.size, { projection.remainingBuiltIns[it].id }) { index ->
                        WorkoutCard(projection.remainingBuiltIns[index], viewModel)
                    }
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
        // Left column: Header fixed, rest scrolls
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fixed header
            Header(viewModel)

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StreakWidget(viewModel)
                NextUpWorkout(viewModel)
            }
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
            val projection = state.workoutProjection

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = R.string.home_next_up.localizable(),
                        modifier = Modifier.padding(bottom = 12.dp),
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Normal,
                    )
                    Text(
                        text = "+",
                        style = Typography.headlineMedium,
                        fontWeight = FontWeight.Normal,
                    )
                }
                projection.yourWorkouts.forEach { workout ->
                    WorkoutCard(workout, viewModel)
                }
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
            val builtInWorkouts = state.workoutProjection.remainingBuiltIns

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = R.string.home_all_workouts.localizable(),
                        modifier = Modifier.padding(bottom = 16.dp),
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )
                }
                items(builtInWorkouts.size, { builtInWorkouts[it].id }) { index ->
                    WorkoutCard(builtInWorkouts[index], viewModel)
                }
            }
        }
    }
}

@Composable
private fun StreakWidget(viewModel: HomeViewModel) {
    when (val state = viewModel.uiStates.collectAsState().value) {
        is UiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        is UiState.Loaded -> {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { viewModel.chartsClicked() }
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
            ) {
                Toolbar(
                    name = R.string.welcome.localizable(state.username ?: ""),
                    modifier = Modifier.weight(1f).padding(12.dp),
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
            val projection = state.workoutProjection

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = R.string.home_next_up.localizable(),
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    Text(
                        text = "+",
                        style = Typography.headlineMedium,
                        fontWeight = FontWeight.Normal,
                    )
                }
                projection.yourWorkouts.forEach { workout ->
                    WorkoutCard(workout, viewModel)
                }
                Text(
                    text = R.string.home_all_workouts.localizable(),
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Normal,
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(projection.remainingBuiltIns.size, { projection.remainingBuiltIns[it].id }) { index ->
                        WorkoutCard(projection.remainingBuiltIns[index], viewModel)
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
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                WorkoutTransitionBounds.update(workout.id, it.boundsInWindow())
            },
        onClick = {
            viewModel.workoutClicked(workout.id)
        }
    ) {
        Text(
            text = workout.displayLabel(),
            modifier = Modifier.padding(8.dp),
            style = Typography.titleMedium,
            color = White
        )
        Text(
            text =R.string.workout_time.localizable(workout.totalTime),
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
