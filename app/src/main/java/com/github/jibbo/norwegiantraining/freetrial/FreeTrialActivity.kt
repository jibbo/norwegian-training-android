package com.github.jibbo.norwegiantraining.freetrial

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.SharedPreferencesSettingsRepository
import com.github.jibbo.norwegiantraining.home.HomeActivity
import com.github.jibbo.norwegiantraining.main.MainActivity
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.DarkPrimary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FreeTrialActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferencesSettingsRepository = SharedPreferencesSettingsRepository(this)
        enableEdgeToEdge()
        setContent {
            NorwegianTrainingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    FreeTrialWelcomeScreen {
                        sharedPreferencesSettingsRepository.startFreeTrial()
                        analytics.logStartFreeTrial(sharedPreferencesSettingsRepository.getFreeTrialEndDate())

                        val intent = Intent(
                            this,
                            HomeActivity::class.java
                        )
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun FreeTrialWelcomeScreen(onStartTrial: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = verticalGradient(
                    colors = listOf(
                        Primary,
                        DarkPrimary
                    )
                )
            )
    ) {
    }
    val giftComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.wrapped_gift))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Icon/Gift Illustration
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(
                    color = Black,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(giftComposition, modifier = Modifier.padding(12.dp))
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    Party(
                        speed = 0f,
                        maxSpeed = 20f,
                        damping = 0.9f,
                        spread = 360,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                        emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).max(100),
                        position = Position.Relative(0.5, 0.3)
                    )
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. The Big Hook
        Text(
            text = R.string.free_trial_title.localizable(),
            style = Typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. The Value Proposition
        Text(
            text = R.string.free_trial_subtitle.localizable(),
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 4. Action Button
        Button(
            onClick = onStartTrial,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Claim My Free Day",
                style = Typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    NorwegianTrainingTheme {
        FreeTrialWelcomeScreen { }
    }
}