package com.github.jibbo.norwegiantraining.components

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
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
fun VideoBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        ExoplayerExample()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Black.copy(alpha = 0.8f)
                )
        )
    }
}

@Composable
fun ExoplayerExample() {
    val context = LocalContext.current
    val videoUri = "android.resource://" + context.packageName + "/" + R.raw.bg
    val mediaItem = remember(videoUri) { // remember MediaItem based on URI
        MediaItem.Builder()
            .setUri(videoUri.toUri())
            .build()
    }
    val exoPlayer = remember(context, mediaItem) {
        ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = true
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                exoPlayer.prepare()
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            StyledPlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun AnimatedToolbar(listState: LazyListState, backDispatcher: OnBackPressedDispatcher? = null) {
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
            text = R.string.title_activity_logs.localizable(),
            style = Typography.displayLarge.copy( // Apply animated font size and line height
                fontSize = animatedFontSizeSp.sp,
                lineHeight = animatedLineHeightSp.sp
            ),
        )
    }
}
