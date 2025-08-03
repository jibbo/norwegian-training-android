package com.github.jibbo.norwegiantraining.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import kotlin.math.absoluteValue

@Composable
internal fun SettingsScreen(
//    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = R.string.title_activity_settings.localizable(),
            style = Typography.displayLarge,
            modifier = modifier
        )

        ProfileCard()

        TTSCard()

    }
}

@Composable
private fun TTSCard() {
    Card {
        Text(
            text = R.string.title_tts_section.localizable(),
            style = Typography.bodyMedium,
            modifier = Modifier.padding(8.dp),
            color = Primary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = R.string.announce_phase.localizable(),
                style = Typography.bodyMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = false, onCheckedChange = {
                // TODO
            })
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = R.string.announce_phase_description.localizable(),
                style = Typography.bodyMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = false, onCheckedChange = {
                // TODO
            })
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = R.string.announce_countdown.localizable(),
                style = Typography.bodyMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = false, onCheckedChange = {
                // TODO
            })
        }
//    HorizontalDivider()
    }
}

@Composable
private fun ProfileCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = R.string.title_profile_section.localizable(),
                style = Typography.bodyMedium,
                color = Primary
            )
            Spacer(modifier = Modifier.weight(1f))
            CircleInitialAvatar("Profile Image")
        }

        TextField(
            placeholder = @Composable {
                Text(text = R.string.your_name.localizable())
            },
            value = "",
            onValueChange = { TODO() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
fun CircleInitialAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp
) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    val backgroundColor = getColorFromName(name)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Text(
            text = initial,
            style = Typography.labelLarge,
            color = Color.Black
        )
    }
}

fun getColorFromName(name: String): Color {
    val colors = listOf(
        Color(0xFFB3E5FC), // Light Blue
        Color(0xFFFFF9C4), // Light Yellow
        Color(0xFFC8E6C9), // Light Green
        Color(0xFFFFCCBC), // Light Orange
        Color(0xFFD1C4E9), // Light Purple
        Color(0xFFFFCDD2)  // Light Red
    )

    val index = (name.hashCode().absoluteValue) % colors.size
    return colors[index]
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    NorwegianTrainingTheme {
        Surface {
            SettingsScreen()
        }
    }
}
