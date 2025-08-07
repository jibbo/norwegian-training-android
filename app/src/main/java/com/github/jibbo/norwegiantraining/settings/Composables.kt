package com.github.jibbo.norwegiantraining.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeTracker
import com.github.jibbo.norwegiantraining.data.FakeUserPreferencesRepo
import com.github.jibbo.norwegiantraining.ui.theme.DarkPrimary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
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

        ProfileCard(viewModel)

        TTSCard(viewModel)

    }
}

@Composable
private fun TTSCard(viewModel: SettingsViewModel) {
    val state = viewModel.uiState.collectAsState()
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
            MySwitch(
                checked = state.value.announcePhase,
                onCheckedChange = {
                    viewModel.setAnnouncePhase(it)
                },
            )
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
            MySwitch(checked = state.value.announcePhaseDesc, onCheckedChange = {
                viewModel.setAnnouncePhaseDesc(it)
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
            MySwitch(checked = state.value.announceCountdown, onCheckedChange = {
                viewModel.setAnnounceCountdown(it)
            })
        }
    }
}

@Composable
private fun ProfileCard(viewModel: SettingsViewModel) {
    val state = viewModel.uiState.collectAsState()
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
        }

        TextField(
            placeholder = @Composable {
                Text(text = R.string.your_name.localizable())
            },
            value = state.value.name ?: "",
            onValueChange = { newValue: String ->
                viewModel.setName(newValue)
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun MySwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = DarkPrimary,
            checkedThumbColor = Primary,
        ),
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    NorwegianTrainingTheme {
        Surface {
            SettingsScreen(SettingsViewModel(FakeUserPreferencesRepo(), FakeTracker()))
        }
    }
}
