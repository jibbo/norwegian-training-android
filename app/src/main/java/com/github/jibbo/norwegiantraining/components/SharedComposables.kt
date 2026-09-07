package com.github.jibbo.norwegiantraining.components

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import kotlin.math.sqrt

private const val GLOW_DURATION_MS = 20_000
private const val GLOW_ALPHA = 0.35f
private const val GLOW_RADIUS_FACTOR = 0.7f
private val GLOW_EASING = EaseInOut
private val GLOW_COLOR = Primary
private val BASE_COLOR = Black
private val GLOW_X_RANGE = 0.25f..0.75f
private val GLOW_Y_RANGE = 0.15f..0.85f

@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BASE_COLOR)
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val phase = rememberGlowPhase()

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GLOW_COLOR.copy(alpha = GLOW_ALPHA),
                            GLOW_COLOR.copy(alpha = 0f)
                        ),
                        center = Offset(
                            x = lerp(GLOW_X_RANGE.start, GLOW_X_RANGE.endInclusive, phase) * width,
                            y = lerp(GLOW_Y_RANGE.start, GLOW_Y_RANGE.endInclusive, phase) * height
                        ),
                        radius = GLOW_RADIUS_FACTOR * sqrt(
                            width.toFloat() * width.toFloat() + height.toFloat() * height.toFloat()
                        )
                    )
                )
        )
    }
}

@Composable
private fun rememberGlowPhase(): Float {
    var phase by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val startNanos = withFrameNanos { it }
        while (true) {
            val frameNanos = withFrameNanos { it }
            val elapsedMillis = (frameNanos - startNanos) / 1_000_000f
            val cyclePosition = elapsedMillis % (GLOW_DURATION_MS * 2f) / GLOW_DURATION_MS
            phase = if (cyclePosition < 1f) {
                GLOW_EASING.transform(cyclePosition)
            } else {
                1f - GLOW_EASING.transform(cyclePosition - 1f)
            }
        }
    }
    return phase
}

@Composable
fun Toolbar(
    name: String,
    backDispatcher: OnBackPressedDispatcher? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = 16.dp)
    ) {
        if (backDispatcher != null) {
            IconButton(onClick = {
                backDispatcher.onBackPressed()
            }) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.outline_arrow_back_24
                    ),
                    contentDescription = R.string.back.localizable(),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = name,
            style = Typography.headlineSmall,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}


@Composable
fun AnimatedToolbar(
    name: String, listState: LazyListState, backDispatcher: OnBackPressedDispatcher? = null
) {
    val density = LocalDensity.current
    val initialFontSizeSp = Typography.displayLarge.fontSize.value
    val targetFontSizeSp = 28f // Target font size in sp, e.g., 28.sp
    val initialLineHeightSp = Typography.displayLarge.lineHeight.value
    val targetLineHeightSp = if (initialFontSizeSp != 0f) {
        targetFontSizeSp * (initialLineHeightSp / initialFontSizeSp)
    } else {
        targetFontSizeSp * 1.2f
    }
    val scrollDistanceToShrinkDp = 60.dp

    val scrollFraction by remember {
        derivedStateOf {
            val scrollOffsetPx = if (listState.firstVisibleItemIndex > 0) {
                with(density) { scrollDistanceToShrinkDp.toPx() }
            } else {
                listState.firstVisibleItemScrollOffset.toFloat()
            }
            val scrollDistanceToShrinkPx = with(density) { scrollDistanceToShrinkDp.toPx() }

            if (scrollDistanceToShrinkPx > 0) {
                (scrollOffsetPx / scrollDistanceToShrinkPx).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }

    // Animate font size
    val animatedFontSizeSp by animateFloatAsState(
        targetValue = lerp(initialFontSizeSp, targetFontSizeSp, scrollFraction),
        label = "fontSizeAnimation"
    )

    // Animate line height
    val animatedLineHeightSp by animateFloatAsState(
        targetValue = lerp(initialLineHeightSp, targetLineHeightSp, scrollFraction),
        label = "lineHeightAnimation"
    )

    Column {
        Spacer(modifier = Modifier.height(animatedLineHeightSp.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            if (backDispatcher != null) {
                IconButton(onClick = {
                    backDispatcher.onBackPressed()
                }) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.outline_arrow_back_24
                        ),
                        contentDescription = R.string.back.localizable(),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = name,
                style = Typography.displayLarge.copy( // Apply animated font size and line height
                    fontSize = animatedFontSizeSp.sp,
                    lineHeight = animatedLineHeightSp.sp
                ),
            )
        }

    }
}
