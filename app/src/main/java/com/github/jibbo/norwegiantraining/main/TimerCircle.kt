package com.github.jibbo.norwegiantraining.main

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
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.delay

@Composable
internal fun WaterCircle(
    state: UiState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .aspectRatio(1f)
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
