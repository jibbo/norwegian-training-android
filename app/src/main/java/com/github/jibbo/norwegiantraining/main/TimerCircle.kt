package com.github.jibbo.norwegiantraining.main

import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
internal fun WaterCircle(
    state: UiState,
    onBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
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
        val circleSide = maxWidth
        val contentScale = (circleSide / 300.dp).coerceIn(0.78f, 1.1f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    fontSize = Typography.titleSmall.fontSize * contentScale,
                    color = Primary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp * contentScale))
            Text(
                text = state.step.name.message().localizable(),
                style = Typography.headlineMedium,
                fontSize = Typography.headlineMedium.fontSize * contentScale,
                color = White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (state.step.name != PhaseName.COMPLETED) {
                Spacer(Modifier.height(8.dp * contentScale))
                Text(
                    text = state.step.name.description().localizable(),
                    style = Typography.bodyMedium,
                    fontSize = Typography.bodyMedium.fontSize * contentScale,
                    color = White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (state.step.name != PhaseName.COMPLETED) {
                Spacer(Modifier.height(12.dp * contentScale))
                CountdownDisplay(
                    targetTimeMillis = state.targetTimeMillis,
                    isRunning = state.isTimerRunning,
                    remainingTimeOnPauseMillis = state.remainingTimeOnPauseMillis,
                    fontSize = (45f * contentScale).coerceIn(32f, 48f).sp
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

private data class Droplet(
    val id: Long,
    val startAngle: Float,
    val centerX: Float,
    val centerY: Float,
    val circleRadius: Float,
    val radius: Float,
    val lingerDurationMillis: Long,
    val slipDurationMillis: Long,
    val dripDurationMillis: Long,
    val progress: Animatable<Float, *>
)

@Composable
internal fun SweatDroplets(
    isTimerRunning: Boolean,
    circleBounds: Rect?,
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
                val centerX = bounds.left + bounds.width / 2f
                val centerY = bounds.top + bounds.height / 2f
                val circleRadius = bounds.width / 2f
                val radiusDp = 6f + Random.nextFloat() * Random.nextFloat() * 10f
                val radiusPx = with(density) { radiusDp.dp.toPx() }
                val slipDuration =
                    (3000L - ((radiusDp - 6f) * 90).toLong()).coerceIn(2100L, 3000L)
                val dripDuration = 2000L
                val droplet = Droplet(
                    id = nextId++,
                    startAngle = Random.nextFloat() * 1.9f - 2.5f,
                    centerX = centerX,
                    centerY = centerY,
                    circleRadius = circleRadius,
                    radius = radiusPx,
                    lingerDurationMillis = 15000L - slipDuration - dripDuration,
                    slipDurationMillis = slipDuration,
                    dripDurationMillis = dripDuration,
                    progress = Animatable(0f)
                )
                droplets.add(droplet)
            }
            delay(5000L)
        }
    }

    droplets.forEach { droplet ->
        LaunchedEffect(droplet.id) {
            val totalMillis = droplet.lingerDurationMillis +
                droplet.slipDurationMillis +
                droplet.dripDurationMillis
            droplet.progress.animateTo(1f, tween(totalMillis.toInt(), easing = LinearEasing))
            droplets.remove(droplet)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (circleBounds == null) return@Canvas
        droplets.forEach { droplet ->
            val totalMillis = droplet.lingerDurationMillis +
                droplet.slipDurationMillis +
                droplet.dripDurationMillis
            val elapsed = droplet.progress.value * totalMillis
            val lingerFraction = (elapsed / droplet.lingerDurationMillis).coerceIn(0f, 1f)
            val slipFraction = ((elapsed - droplet.lingerDurationMillis) / droplet.slipDurationMillis)
                .coerceIn(0f, 1f)
            val dripFraction = ((elapsed - droplet.lingerDurationMillis - droplet.slipDurationMillis) /
                droplet.dripDurationMillis).coerceIn(0f, 1f)

            val bottomAngle = (PI / 2.0).toFloat()
            val targetAngle = if (droplet.startAngle <= -bottomAngle) {
                -3f * bottomAngle
            } else {
                bottomAngle
            }
            val slipPos = 0.25f * slipFraction + 0.75f * slipFraction * slipFraction
            val angle = droplet.startAngle + (targetAngle - droplet.startAngle) * slipPos

            val radius = droplet.radius * (0.5f + 0.5f * lingerFraction)

            if (slipFraction > 0f && dripFraction <= 0f) {
                drawTrail(
                    droplet = droplet,
                    startAngle = droplet.startAngle,
                    angle = angle,
                    trailProgress = slipFraction,
                    radius = radius
                )
            }

            val wobble =
                sin(slipFraction * PI.toFloat() * 4f) * droplet.radius * 0.15f * (1f - slipFraction)
            val wobbleRadius = droplet.circleRadius + wobble
            var x = droplet.centerX + wobbleRadius * cos(angle)
            var y = droplet.centerY + wobbleRadius * sin(angle)
            if (dripFraction > 0f) {
                val dripDrop = dripFraction * dripFraction
                x = droplet.centerX
                y = droplet.centerY + droplet.circleRadius + dripDrop * droplet.circleRadius * 0.5f
            }

            val alpha = when {
                lingerFraction < 1f -> lingerFraction * 0.9f
                dripFraction > 0f -> (1f - dripFraction) * 0.85f
                else -> 0.85f
            }
            val degrees = 180f / PI.toFloat()
            val trailEase =
                if (slipFraction <= 0f) 0f else 1f - (1f - slipFraction) * (1f - slipFraction)
            val motionSign = if (targetAngle > droplet.startAngle) 1f else -1f
            val tipFlip = if (motionSign < 0f) PI.toFloat() else 0f
            val travelRotation = (angle + tipFlip) * trailEase * (1f - dripFraction)
            rotate(degrees = travelRotation * degrees, pivot = Offset(x, y)) {
                drawTeardrop(
                    x = x,
                    y = y,
                    radius = radius,
                    alpha = alpha,
                    stretchY = 1f + 0.2f * slipFraction
                )
            }
        }
    }
}

private fun DrawScope.drawTrail(
    droplet: Droplet,
    startAngle: Float,
    angle: Float,
    trailProgress: Float,
    radius: Float
) {
    val degrees = 180f / PI.toFloat()
    val topLeft = Offset(
        droplet.centerX - droplet.circleRadius,
        droplet.centerY - droplet.circleRadius
    )
    val size = Size(droplet.circleRadius * 2f, droplet.circleRadius * 2f)
    val steps = 10
    for (i in 0 until steps) {
        val f0 = i.toFloat() / steps
        val f1 = (i + 1f) / steps
        val a0 = angle + (startAngle - angle) * f0
        val a1 = angle + (startAngle - angle) * f1
        drawArc(
            color = Primary,
            startAngle = a0 * degrees,
            sweepAngle = (a1 - a0) * degrees,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = radius * 1.1f, cap = StrokeCap.Round),
            alpha = 0.12f * (1f - f0) * trailProgress
        )
    }
}

private fun DrawScope.drawTeardrop(
    x: Float,
    y: Float,
    radius: Float,
    alpha: Float,
    stretchY: Float = 1f
) {
    if (radius <= 0f) return
    val length = radius * 2.4f * stretchY
    val halfWidth = radius * 1.1f
    val tipY = y - length * 0.55f
    val bottomY = y + length * 0.42f
    val path = Path().apply {
        moveTo(x, tipY)
        cubicTo(
            x + halfWidth * 0.6f, y - length * 0.15f,
            x + halfWidth * 0.75f, y + length * 0.3f,
            x, bottomY
        )
        cubicTo(
            x - halfWidth * 0.75f, y + length * 0.3f,
            x - halfWidth * 0.6f, y - length * 0.15f,
            x, tipY
        )
        close()
    }
    val highlight = Offset(x - halfWidth * 0.3f, y - length * 0.05f)
    drawPath(
        path = path,
        color = Primary.copy(alpha = 0.45f),
        style = Stroke(width = radius * 0.35f, cap = StrokeCap.Round),
        alpha = alpha
    )
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                White.copy(alpha = 0.5f),
                Primary.copy(alpha = 0.3f),
                Primary.copy(alpha = 0.1f)
            ),
            center = highlight,
            radius = radius * 1.3f
        ),
        alpha = alpha
    )
    drawCircle(
        color = White,
        radius = radius * 0.24f,
        center = Offset(x - halfWidth * 0.4f, y - length * 0.22f),
        alpha = alpha * 0.85f
    )
    drawCircle(
        color = White,
        radius = radius * 0.1f,
        center = Offset(x - halfWidth * 0.55f, y - length * 0.32f),
        alpha = alpha * 0.7f
    )
    drawCircle(
        color = White,
        radius = radius * 0.12f,
        center = Offset(x + halfWidth * 0.35f, y + length * 0.2f),
        alpha = alpha * 0.25f
    )
}
