package com.github.jibbo.norwegiantraining.paywall

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.components.VideoBackground
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Typography

class PaywallActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NorwegianTrainingTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = verticalGradient(
                                    colors = listOf(
                                        Color.DarkGray,
                                        Black
                                    )
                                )
                            )
                    ) {
                        if (!LocalInspectionMode.current) {
                            VideoBackground()
                        }
                        Content()
                    }
                }
            }
        }
    }

    @Composable
    private fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Unlock Norwegian Training",
                style = Typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
            Advantage(
                text = "Stress free training",
            )
            Advantage(
                text = "Voice assisted workout",
            )
            Advantage(
                text = "Guidance and Structure",
            )
            Advantage(
                text = "Progress Tracking and Data Analysis",
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(64.dp)
            ) {
                Text(
                    text = "0.99 USD / Month",
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick = {
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(64.dp)
            ) {
                Text(
                    text = "2.99 USD Lifetime",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    @Composable
    fun Advantage(text: String, modifier: Modifier = Modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(
                    id = R.drawable.baseline_check_circle_outline_24,
                ),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = Typography.bodyLarge,
                modifier = modifier
                    .fillMaxWidth()
            )
        }
    }

    @Composable
    @Preview
    fun Preview() {
        NorwegianTrainingTheme {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Content()
            }
        }
    }
}
