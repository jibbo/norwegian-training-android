package com.github.jibbo.norwegiantraining.main

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.VideoBackground
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSessionRepo
import com.github.jibbo.norwegiantraining.data.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.data.FakeWorkoutRepo
import com.github.jibbo.norwegiantraining.domain.GetTodaySessionUseCase
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.domain.MoveToNextPhaseDomainService
import com.github.jibbo.norwegiantraining.domain.PhaseEndedUseCase
import com.github.jibbo.norwegiantraining.domain.SkipPhaseUseCase
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Red
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
internal fun MainView(
    mainViewModel: MainViewModel,
) {
    val state by mainViewModel.uiStates.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        if (!LocalInspectionMode.current) {
            VideoBackground()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(horizontal = 16.dp)
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
    Spacer(modifier = Modifier.height(64.dp))
    Text(
        text = state.step.name.message().localizable(),
        style = Typography.headlineLarge,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    Text(
        text = state.step.name.description().localizable(),
        style = Typography.headlineSmall,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f)
            .padding(16.dp),
        textAlign = TextAlign.Center,
    )
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
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.safeDrawingPadding()) {
        Text(
            text = R.string.welcome.localizable(state.name),
            style = Typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { viewModel.closeWorkout() }) {
            Icon(
                painter = painterResource(R.drawable.outline_close_24),
                contentDescription = ""
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreview() {
    NorwegianTrainingTheme {
        val sessionRepository = FakeSessionRepo()
        val settingsRepository = FakeSettingsRepository()
        val workoutRepository = FakeWorkoutRepo()
        val getTodaySession = GetTodaySessionUseCase(sessionRepository)
        MainView(
            mainViewModel = MainViewModel(
                MoveToNextPhaseDomainService(workoutRepository),
                getTodaySession,
                PhaseEndedUseCase(getTodaySession, sessionRepository),
                SkipPhaseUseCase(getTodaySession, sessionRepository),
                GetUsername(settingsRepository),
                settingsRepository
            ),
        )
    }
}
