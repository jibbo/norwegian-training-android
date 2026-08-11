package com.github.jibbo.norwegiantraining.main

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.BuildConfig
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Red
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
internal fun MainView(
    mainViewModel: MainViewModel,
) {
    val state by mainViewModel.uiStates.collectAsState()
    val circleBounds = remember { mutableStateOf<Rect?>(null) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Black,
    ) { innerPadding ->
        val bottomInsetPx = with(LocalDensity.current) { innerPadding.calculateBottomPadding().toPx() }
        Box(modifier = Modifier.fillMaxSize()) {
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
                Header(viewModel = mainViewModel, isDebugMode = BuildConfig.DEBUG)
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    WaterCircle(state = state, onBoundsChanged = { circleBounds.value = it })
                }
                Spacer(modifier = Modifier.weight(1f))
                val animatedBackgroundColor by animateColorAsState(
                    targetValue = if (state.isTimerRunning) Red else Primary,
                    label = "ButtonBackgroundColorAnimation"
                )
                Button(
                    onClick = {
                        mainViewModel.onMainButtonClicked()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = animatedBackgroundColor),
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    val text = state.mainButtonText.localizable().uppercase()
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
            SweatDroplets(
                isTimerRunning = state.isTimerRunning,
                circleBounds = circleBounds.value,
                bottomInsetPx = bottomInsetPx,
                modifier = Modifier.fillMaxSize()
            )
            if (state.showConfetti) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize(),
                    parties = listOf(
//                    Party(
//                        speed = 0f,
//                        maxSpeed = 15f,
//                        damping = 0.9f,
//                        angle = Angle.BOTTOM,
//                        spread = Spread.ROUND,
//                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
//                        emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(100),
//                        position = Position.Relative(0.0, 0.0)
//                            .between(Position.Relative(1.0, 0.0))
//                    )
                        Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                            position = Position.Relative(0.5, 0.3)
                        )
                    )
                )
            }
        }
    }
}

@Composable
internal fun Header(viewModel: MainViewModel, isDebugMode: Boolean) {
    val state by viewModel.uiStates.collectAsState()
    var isDebugMenuExpanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = R.string.home_workout_name.localizable(state.workoutName),
            style = Typography.headlineLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isDebugMode) {
            IconButton(onClick = { isDebugMenuExpanded = !isDebugMenuExpanded }) {
                Icon(
                    painter = painterResource(R.drawable.baseline_settings_24),
                    contentDescription = "Debug options"
                )
            }
            DropdownMenu(
                expanded = isDebugMenuExpanded,
                onDismissRequest = { isDebugMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        isDebugMenuExpanded = false
                        viewModel.debugShowConfetti()
                    },
                    text = { Text("Show confetti") }
                )
                DropdownMenuItem(
                    onClick = {
                        isDebugMenuExpanded = false
                        viewModel.debugShowLevelUp()
                    },
                    text = { Text("Show Level up") }
                )
                DropdownMenuItem(
                    onClick = {
                        isDebugMenuExpanded = false
                        viewModel.debugCompleteWorkout()
                    },
                    text = { Text("Complete workout") }
                )
            }
        }
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
        MainView(
            mainViewModel = MainViewModel(),
        )
    }
}
