package com.github.jibbo.norwegiantraining.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.ui.theme.Black
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
