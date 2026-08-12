package com.github.jibbo.norwegiantraining.components

import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView

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
    name: String,
    listState: LazyListState
) {
    val density = LocalDensity.current
    val initialFontSizeSp = Typography.displayLarge.fontSize.value
    val targetFontSizeSp = 28f
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

    val animatedFontSizeSp by animateFloatAsState(
        targetValue = lerp(initialFontSizeSp, targetFontSizeSp, scrollFraction),
        label = "fontSizeAnimation"
    )
    val animatedLineHeightSp by animateFloatAsState(
        targetValue = lerp(initialLineHeightSp, targetLineHeightSp, scrollFraction),
        label = "lineHeightAnimation"
    )

    Text(
        text = name,
        style = Typography.displayLarge.copy(
            fontSize = animatedFontSizeSp.sp,
            lineHeight = animatedLineHeightSp.sp
        ),
        modifier = Modifier.padding(start = 6.dp)
    )
}

@Composable
fun BackToolbar(
    name: String,
    listState: LazyListState,
    backDispatcher: OnBackPressedDispatcher? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        if (backDispatcher != null) {
            IconButton(
                onClick = {
                    backDispatcher.onBackPressed()
                },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.outline_arrow_back_24
                    ),
                    contentDescription = R.string.back.localizable(),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        AnimatedToolbar(name, listState)
    }
}
