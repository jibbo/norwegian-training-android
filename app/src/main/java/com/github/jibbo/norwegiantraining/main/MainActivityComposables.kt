package com.github.jibbo.norwegiantraining.main

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSessionRepo
import com.github.jibbo.norwegiantraining.data.FakeUserPreferencesRepo
import com.github.jibbo.norwegiantraining.domain.GetTodaySessionUseCase
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.domain.MoveToNextPhaseDomainService
import com.github.jibbo.norwegiantraining.domain.SaveTodaySession
import com.github.jibbo.norwegiantraining.domain.SkipPhaseUseCase
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Oswald
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Red
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
internal fun MainView(
    mainViewModel: MainViewModel,
) {
    val state by mainViewModel.uiStates.collectAsState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (!LocalInspectionMode.current) {
            VideoBackground()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            Header(viewModel = mainViewModel)
            Spacer(modifier = Modifier.weight(1f))
            Instructions(state)
            Spacer(modifier = Modifier.weight(1f))
            if (mainViewModel.showCountdown()) {
                Timer(state, mainViewModel)
            }

            val animatedBackgroundColor by animateColorAsState(
                targetValue = if (state.isTimerRunning) Red else Primary,
                label = "ButtonBackgroundColorAnimation"
            )
            Button(
                onClick = { mainViewModel.mainButtonClicked() },
                colors = ButtonDefaults.buttonColors(containerColor = animatedBackgroundColor),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .imePadding()
            ) {
                val text = if (state.isTimerRunning)
                    R.string.pause.localizable().uppercase()
                else R.string.start.localizable().uppercase()
                val textColor: Color = if (state.isTimerRunning) White else Black
                Text(
                    text = text,
                    style = Typography.titleLarge,
                    color = textColor,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (mainViewModel.showSkipButton()) {
                TextButton(onClick = {
                    mainViewModel.skipClicked()
                }) {
                    Text(
                        text = R.string.skip.localizable(),
                        style = Typography.titleMedium,
                        color = White,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.Timer(
    state: UiState,
    mainViewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CountdownDisplay(
            targetTimeMillis = state.targetTimeMillis,
            isRunning = state.isTimerRunning,
            onFinish = {
                mainViewModel.onTimerFinish()
            }
        )
    }

    Spacer(modifier = Modifier.weight(1f))
}

@Composable
private fun Instructions(state: UiState) {
    Text(
        text = state.step.message().localizable(),
        style = Typography.headlineLarge,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    Text(
        text = state.step.description().localizable(),
        style = Typography.headlineSmall,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f)
            .padding(16.dp),
        textAlign = TextAlign.Center,
    )
// TODO
//    Text(
//        text = R.string.next_up.localizable(state.nextMessage().localizable()),
//        style = Typography.labelLarge,
//        modifier = Modifier
//            .fillMaxWidth()
//            .alpha(0.8f),
//        textAlign = TextAlign.Center,
//    )
}

@Composable
private fun VideoBackground() {
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
internal fun CountdownDisplay(
    targetTimeMillis: Long,
    isRunning: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingTimeMillis by remember(targetTimeMillis, isRunning) {
        mutableStateOf(
            targetTimeMillis - System.currentTimeMillis()
        )
    }

    LaunchedEffect(key1 = targetTimeMillis, key2 = isRunning) {
        if (!isRunning) {
            return@LaunchedEffect
        }
        // Recalculate initial remaining time when targetTimeMillis or isRunning changes to true
        remainingTimeMillis = targetTimeMillis - System.currentTimeMillis()

        while (isRunning && remainingTimeMillis > 0) {
            delay(1000L) // Update every second
            remainingTimeMillis = targetTimeMillis - System.currentTimeMillis()
        }
        if (remainingTimeMillis <= 0) {
            onFinish()
        }
    }

    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis.coerceAtLeast(0L))
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeMillis.coerceAtLeast(0L)) % 60

    Text(
        text = String.format("%02d:%02d", minutes, seconds),
        style = Typography.displayLarge,
        fontSize = 128.sp,
        modifier = modifier
    )
}

@Composable
internal fun Header(viewModel: MainViewModel) {
    val state by viewModel.uiStates.collectAsState()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = R.string.welcome.localizable(state.name),
            fontFamily = Oswald,
            fontWeight = FontWeight.ExtraLight,
            fontSize = 24.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            viewModel.chartsClicked()
        }) {
            Icon(
                painter = painterResource(R.drawable.outline_area_chart_24),
                contentDescription = ""
            )
        }
        IconButton(onClick = {
            viewModel.settingsClicked()
        }) {
            Icon(
                painter = painterResource(R.drawable.baseline_settings_24),
                contentDescription = ""
            )
        }
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreview() {
    NorwegianTrainingTheme {
        val sessionRepository = FakeSessionRepo()
        val settingsRepository = FakeUserPreferencesRepo()
        val getTodaySession = GetTodaySessionUseCase(sessionRepository)
        MainView(
            mainViewModel = MainViewModel(
                MoveToNextPhaseDomainService(
                    getTodaySession,
                    SaveTodaySession(sessionRepository)
                ),
                getTodaySession,
                SkipPhaseUseCase(getTodaySession, sessionRepository),
                GetUsername(settingsRepository),
                settingsRepository
            ),
        )
    }
}
