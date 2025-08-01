package com.github.jibbo.norwegiantraining

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun MainView(
    mainViewModel: MainViewModel,
) {
    val state by mainViewModel.uiStates.collectAsState()
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Welcome!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = """ We are going to do a classic Norwegian training:\n
                1.10 minutes of warm-up\n
                2. 4 Minutes of high intensity cardio (85-95%) max heart rate\n
                3. 4 Minutes of low intensity cardio (55-65%) max heart rate\n
                We will repeat step 2 and 3  for 4 times\n.
                Are you ready?
            """".trimMargin(),
        )
        Button(onClick = { mainViewModel.scheduleNextAlarm() }) {
            Text(text = if (state.isTimerRunning) "Stop" else "Start")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Step is ${state.step}")
        if (state.isTimerRunning) {
            CountdownDisplay(
                targetTimeMillis = state.targetTimeMillis,
                isRunning = state.isTimerRunning, // Pass this to control the effect
                onFinish = {
                    mainViewModel.onTimerFinish()
                }
            )
        }
    }
}

@Composable
fun CountdownDisplay(
    targetTimeMillis: Long,
    isRunning: Boolean,
    onFinish: () -> Unit
) {
    var remainingTimeMillis by remember { mutableStateOf(targetTimeMillis - System.currentTimeMillis()) }

    LaunchedEffect(key1 = targetTimeMillis, key2 = isRunning) {
        if (!isRunning) {
            // If timer is stopped externally, ensure remainingTimeMillis is updated or cleared
            remainingTimeMillis = 0L // Or keep the last value if preferred
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
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NorwegianTrainingTheme {
        // Provide dummy values for preview
        MainView(
            mainViewModel = MainViewModel(),
        )
    }
}
