package com.github.jibbo.norwegiantraining.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.domain.PhaseName
import com.github.jibbo.norwegiantraining.ui.theme.*
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
internal fun WaterCircle(
    state: UiState,
    onBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .aspectRatio(1f)
            .onGloballyPositioned { coordinates ->
                onBoundsChanged(
                    Rect(
                        coordinates.positionInRoot().x,
                        coordinates.positionInRoot().y,
                        coordinates.positionInRoot().x + coordinates.size.width.toFloat(),
                        coordinates.positionInRoot().y + coordinates.size.height.toFloat()
                    )
                )
            }
            .shadow(16.dp, CircleShape, spotColor = Primary.copy(alpha = 0.45f))
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(WaterShallow.copy(alpha = 0.55f), WaterDeep),
                    center = Offset(0.5f, 0.5f),
                    radius = 0.5f
                )
            )
            .border(2.dp, Primary.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.step.name != PhaseName.COMPLETED) {
                val phasesText: String = when {
                    state.currentPhaseIndex == 0 -> {
                        R.string.phases_total.localizable(state.totalPhases)
                    }
                    state.currentPhaseIndex <= state.totalPhases -> {
                        R.string.current_phases.localizable(state.currentPhaseIndex, state.totalPhases)
                    }
                    else -> {
                        R.string.current_phases.localizable(state.totalPhases, state.totalPhases)
                    }
                }
                Text(
                    text = phasesText.uppercase(),
                    style = Typography.titleSmall,
                    color = Primary,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.step.name.message().localizable(),
                style = Typography.headlineMedium,
                color = White,
                textAlign = TextAlign.Center
            )
            if (state.step.name != PhaseName.COMPLETED) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.step.name.description().localizable(),
                    style = Typography.bodyMedium,
                    color = White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (state.step.name != PhaseName.COMPLETED) {
                Spacer(Modifier.height(12.dp))
                CountdownDisplay(
                    targetTimeMillis = state.targetTimeMillis,
                    isRunning = state.isTimerRunning,
                    remainingTimeOnPauseMillis = state.remainingTimeOnPauseMillis,
                    fontSize = 45.sp
                )
            }
        }
    }
}

@Composable
internal fun CountdownDisplay(
    targetTimeMillis: Long,
    isRunning: Boolean,
    remainingTimeOnPauseMillis: Long = 0L,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 128.sp
) {
    var remainingTimeMillis by remember(targetTimeMillis, remainingTimeOnPauseMillis) {
        mutableStateOf(
            if (isRunning) {
                (targetTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            } else {
                remainingTimeOnPauseMillis
            }
        )
    }

    LaunchedEffect(key1 = isRunning, key2 = targetTimeMillis) {
        if (!isRunning) {
            remainingTimeMillis = remainingTimeOnPauseMillis
            return@LaunchedEffect
        }

        while (isRunning) {
            remainingTimeMillis = (targetTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            delay(1000L)
        }
    }

    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis.coerceAtLeast(0L))
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeMillis.coerceAtLeast(0L)) % 60

    Text(
        text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
        style = Typography.displayLarge,
        fontSize = fontSize,
        modifier = modifier
    )
}

private data class SplashSpark(
    val velocityX: Float,
    val velocityY: Float,
    val radius: Float
)

private data class Droplet(
    val id: Long,
    val startX: Float,
    val radius: Float,
    val growDurationMillis: Long,
    val dropDurationMillis: Long,
    val splashDurationMillis: Long,
    val progress: Animatable<Float, *>,
    val splash: Animatable<Float, *>,
    val sparks: List<SplashSpark>
)

@Composable
internal fun SweatDroplets(
    isTimerRunning: Boolean,
    circleBounds: Rect?,
    bottomInsetPx: Float,
    modifier: Modifier = Modifier
) {
    val droplets = remember { mutableStateListOf<Droplet>() }
    var nextId by remember { mutableLongStateOf(0L) }
    val density = LocalDensity.current
    val currentBounds by rememberUpdatedState(circleBounds)

    LaunchedEffect(isTimerRunning) {
        if (!isTimerRunning) return@LaunchedEffect
        while (true) {
            val bounds = currentBounds
            if (bounds != null) {
                val radiusPx = with(density) { (10f + Random.nextFloat() * 16f).dp.toPx() }
                val droplet = Droplet(
                    id = nextId++,
                    startX = Random.nextFloat() * bounds.width,
                    radius = radiusPx,
                    growDurationMillis = Random.nextLong(2000L, 2600L),
                    dropDurationMillis = Random.nextLong(700L, 900L),
                    splashDurationMillis = Random.nextLong(600L, 700L),
                    progress = Animatable(0f),
                    splash = Animatable(0f),
                    sparks = List(8) {
                        SplashSpark(
                            velocityX = Random.nextFloat() * 440f - 220f,
                            velocityY = -Random.nextFloat() * 260f - 120f,
                            radius = with(density) { (2.5f + Random.nextFloat() * 3.5f).dp.toPx() }
                        )
                    }
                )
                droplets.add(droplet)
            }
            delay(1000L)
        }
    }

    droplets.forEach { droplet ->
        LaunchedEffect(droplet.id) {
            droplet.progress.animateTo(0.55f, tween(droplet.growDurationMillis.toInt(), easing = LinearEasing))
            droplet.progress.animateTo(1f, tween(droplet.dropDurationMillis.toInt(), easing = EaseIn))
            droplet.splash.animateTo(1f, tween(droplet.splashDurationMillis.toInt(), easing = LinearEasing))
            droplets.remove(droplet)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (circleBounds == null) return@Canvas
        val bottomY = size.height - bottomInsetPx
        droplets.forEach { droplet ->
            val progress = droplet.progress.value
            when {
                progress < 0.55f -> {
                    val growFraction = (progress / 0.55f).coerceIn(0f, 1f)
                    val radius = droplet.radius * growFraction * 1.05f
                    val alpha = (growFraction * 1.2f).coerceIn(0f, 1f)
                    drawDroplet(
                        x = circleBounds.left + droplet.startX,
                        y = circleBounds.top,
                        radius = radius,
                        alpha = alpha
                    )
                }

                progress < 1f -> {
                    val dropFraction = ((progress - 0.55f) / 0.45f).coerceIn(0f, 1f)
                    val x = circleBounds.left + droplet.startX
                    val y = circleBounds.top + (bottomY - circleBounds.top) * dropFraction
                    drawDroplet(
                        x = x,
                        y = y,
                        radius = droplet.radius,
                        alpha = 0.95f,
                        stretchY = 1.1f + 0.3f * dropFraction
                    )
                }

                else -> {
                    val splashFraction = droplet.splash.value
                    val impactX = circleBounds.left + droplet.startX
                    val impactY = bottomY
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Primary.copy(alpha = 1f), Primary.copy(alpha = 0f))
                        ),
                        radius = droplet.radius * (1.2f + splashFraction * 1.6f),
                        center = Offset(impactX, impactY),
                        alpha = (1f - splashFraction) * 0.7f
                    )
                    droplet.sparks.forEach { spark ->
                        val t = splashFraction * (droplet.splashDurationMillis / 1000f)
                        val splashGravity = 1600f
                        val sx = impactX + spark.velocityX * t
                        val sy = impactY + spark.velocityY * t + 0.5f * splashGravity * t * t
                        drawCircle(
                            color = Primary,
                            radius = spark.radius * (1f - 0.4f * splashFraction),
                            center = Offset(sx, sy),
                            alpha = (1f - splashFraction) * 0.9f
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawDroplet(
    x: Float,
    y: Float,
    radius: Float,
    alpha: Float,
    stretchY: Float = 1f
) {
    if (radius <= 0f) return
    val highlightOffset = radius * 0.3f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Primary.copy(alpha = 0.95f), Primary.copy(alpha = 0.1f)),
            center = Offset(x - highlightOffset, y - highlightOffset * 0.7f),
            radius = radius * 1.2f
        ),
        topLeft = Offset(x - radius, y - radius * stretchY),
        size = Size(radius * 2f, radius * 2f * stretchY),
        alpha = alpha
    )
    drawCircle(
        color = White,
        radius = radius * 0.22f,
        center = Offset(x - highlightOffset, y - highlightOffset),
        alpha = alpha * 0.55f
    )
}
