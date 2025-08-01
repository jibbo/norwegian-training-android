package com.github.jibbo.norwegiantraining

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            Text(
                text = "Welcome!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.stepsMessage().localizable(),
                    fontSize = 32.sp,
                )
                CountdownDisplay(
                    targetTimeMillis = state.targetTimeMillis,
                    isRunning = state.isTimerRunning,
                    onFinish = {
                        mainViewModel.onTimerFinish()
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { mainViewModel.scheduleNextAlarm() },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .imePadding()
            ) {
                val text = if (state.isTimerRunning)
                    R.string.stop.localizable().uppercase()
                else R.string.start.localizable().uppercase()
                Text(
                    text = text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun CountdownDisplay(
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
        fontSize = 64.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
        // No explicit color needed, should default to onBackground from Surface
    )
}

@Composable
fun Int.localizable() = stringResource(this)


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreview() {
    NorwegianTrainingTheme {
        MainView(
            mainViewModel = MainViewModel(),
        )
    }
}
