package com.github.jibbo.norwegiantraining.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.UserPreferencesRepo
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            Header(viewModel = mainViewModel)
            if (!mainViewModel.showCountdown()) {
                Spacer(modifier = Modifier.weight(1f))
            }
            Text(
                text = state.stepMessage().localizable(),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = state.description().localizable(),
                fontSize = 22.sp,
                lineHeight = 26.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.8f)
                    .padding(16.dp),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (mainViewModel.showCountdown()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.weight(1f))
            }
            Text(
                text = "Next Up >> ${state.nextMessage().localizable()}",
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.8f)
                    .padding(16.dp),
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = { mainViewModel.mainButtonClicked() },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .imePadding()
            ) {
                val text = if (state.isTimerRunning)
                    R.string.pause.localizable().uppercase()
                else R.string.start.localizable().uppercase()
                Text(
                    text = text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (mainViewModel.showSkipButton()) {
                TextButton(onClick = {
                    mainViewModel.skipClicked()
                }) {
                    Text(
                        text = R.string.skip.localizable(),
                        fontSize = 22.sp,
                        lineHeight = 26.sp,
                        color = Primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(0.8f)
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
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
        fontSize = 64.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
        // No explicit color needed, should default to onBackground from Surface
    )
}

@Composable
internal fun Header(viewModel: MainViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.weight(1f))
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
    Spacer(modifier = Modifier.height(100.dp))
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreview() {
    NorwegianTrainingTheme {
        MainView(
            mainViewModel = MainViewModel(FakeUserPreferencesRepo(), FakeSessionRepo()),
        )
    }
}

class FakeSessionRepo : SessionRepository {
    override suspend fun getSessions(
        limit: Int,
        offset: Int
    ): List<Session> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertSession(session: Session) {
        TODO("Not yet implemented")
    }

    override suspend fun getTodaySession(): Session? {
        TODO("Not yet implemented")
    }

}

class FakeUserPreferencesRepo : UserPreferencesRepo {
    override fun setUserName(name: String?) {
        TODO("Not yet implemented")
    }

    override fun getUserName(): String? {
        TODO("Not yet implemented")
    }

    override fun setAnnouncePhase(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnouncePhase(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setAnnouncePhaseDesc(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnouncePhaseDesc(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setAnnounceCountdown(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnounceCountdown(): Boolean {
        TODO("Not yet implemented")
    }

}
