package com.github.jibbo.norwegiantraining.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.github.jibbo.norwegiantraining.BuildConfig
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.AnimatedToolbar
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.data.FakeTracker
import com.github.jibbo.norwegiantraining.onboarding.OnboardingActivity
import com.github.jibbo.norwegiantraining.paywall.PaywallActivity
import com.github.jibbo.norwegiantraining.ui.theme.DarkPrimary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
    innerPadding: PaddingValues
) {
    val listState = rememberLazyListState()
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
    ) {
        AnimatedToolbar(
            R.string.title_activity_settings.localizable(), listState,
            LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = listState,
        ) {
            item { ProfileCard(viewModel) }
            item { TTSCard(viewModel) }
            item { OnboardingCard(viewModel) }
            item { BetaCard(viewModel) }
            item { PrivacyCard(viewModel) }
            item { GetInTouchCard() }
            item { CreditsCard() }

            if (BuildConfig.DEBUG) {
                item { DebugCard() }
            }
        }
    }
}

@Composable
private fun TTSCard(viewModel: SettingsViewModel) {
    val state = viewModel.uiState.collectAsState()
    Card {
        Column(modifier = Modifier.padding(6.dp)) {
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
                    modifier = Modifier.weight(1f)
                )
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
                    modifier = Modifier.weight(1f)
                )
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
                    modifier = Modifier.weight(1f)
                )
                MySwitch(checked = state.value.announceCountdown, onCheckedChange = {
                    viewModel.setAnnounceCountdown(it)
                })
            }
        }
    }
}

@Composable
private fun ProfileCard(viewModel: SettingsViewModel) {
    val state = viewModel.uiState.collectAsState()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(6.dp)) {
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
}

@Composable
private fun BetaCard(viewModel: SettingsViewModel) {
    val state = viewModel.uiState.collectAsState()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(6.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = R.string.title_beta_section.localizable(),
                    style = Typography.bodyMedium,
                    color = Primary
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Text(
                text = R.string.beta_section_description.localizable(),
                style = Typography.labelMedium,
                color = White.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(all = 16.dp)
            ) {
                Text(
                    text = R.string.enable_timer_notification.localizable(),
                    style = Typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                MySwitch(
                    checked = state.value.isTimerNotificationEnabled,
                    onCheckedChange = {
                        viewModel.toggleTimerNotification(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun GetInTouchCard() {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(6.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = R.string.about_dev_section_title.localizable(),
                    style = Typography.bodyMedium,
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(id = R.mipmap.me),
                    modifier = Modifier
                        .clip(shape = CircleShape)
                        .size(24.dp),
                    contentDescription = null,
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = R.string.about_dev_section_description.localizable(),
                    style = Typography.bodyMedium,
                    color = White.copy(alpha = 0.6f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.drawable.baseline_email_24),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.size(6.dp))
                TextButton(onClick = {
                    context.composeEmail()
                }) {
                    Text(
                        text = R.string.about_dev_section_cta.localizable(),
                        style = Typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingCard(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val intent = Intent(
        context,
        OnboardingActivity::class.java
    )
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                TextButton(
                    onClick = {
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = R.string.onboarding_section_title.localizable(),
                        style = Typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PrivacyCard(viewModel: SettingsViewModel) {
    val state = viewModel.uiState.collectAsState()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(6.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = R.string.title_privacy_section.localizable(),
                    style = Typography.bodyMedium,
                    color = Primary
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(all = 16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = R.string.enable_crash_reporting.localizable(),
                        style = Typography.bodyMedium,
                    )
                    Text(
                        text = R.string.enable_crash_reporting_description.localizable(),
                        style = Typography.labelMedium,
                        color = White.copy(alpha = 0.6f),
                    )
                }
                MySwitch(
                    checked = state.value.isCrashReportingEnabled,
                    onCheckedChange = {
                        viewModel.toggleCrashReporting(it)
                    },
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(all = 16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = R.string.enable_analytics.localizable(),
                        style = Typography.bodyMedium,
                    )
                    Text(
                        text = R.string.enable_analytics_description.localizable(),
                        style = Typography.labelMedium,
                        color = White.copy(alpha = 0.6f),
                    )
                }
                MySwitch(
                    checked = state.value.isAnalyticsEnabled,
                    onCheckedChange = {
                        viewModel.toggleAnalytics(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun CreditsCard() {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(6.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = R.string.credits_section_cta.localizable(),
                    style = Typography.bodyMedium,
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Illustrations by ")
                        withLink(
                            LinkAnnotation.Url(
                                "https://twitter.com/ninalimpi",
                                TextLinkStyles(style = SpanStyle(color = Primary))
                            )
                        ) {
                            append("Katerina Limpitsouni")
                        }
                    },
                    style = Typography.bodyMedium,
                    color = White.copy(alpha = 0.6f)
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Video by ")
                        withLink(
                            LinkAnnotation.Url(
                                "https://www.pexels.com/video/woman-running-through-the-stairs-3048202/",
                                TextLinkStyles(style = SpanStyle(color = Primary))
                            )
                        ) {
                            append("Fauxels")
                        }
                    },
                    style = Typography.bodyMedium,
                    color = White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun DebugCard() {
    val context = LocalContext.current
    val intent = Intent(
        context,
        PaywallActivity::class.java
    )
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "DEBUG - Solo per Gio 🚫",
                    style = Typography.bodyMedium,
                    color = Primary
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                TextButton(
                    onClick = {
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "Paywall (remove .debug suffix from .gradle)",
                        style = Typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MySwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = Primary,
            checkedThumbColor = DarkPrimary,
        ),
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    NorwegianTrainingTheme {
        Surface {
            Scaffold { innerPadding ->
                SettingsScreen(
                    SettingsViewModel(FakeSettingsRepository(), FakeTracker()),
                    innerPadding
                )
            }
        }
    }
}

fun Context.composeEmail() {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri() // Only email apps handle this.
        putExtra(Intent.EXTRA_EMAIL, arrayOf("info@jibbo.it"))
        putExtra(Intent.EXTRA_SUBJECT, "Feedback on Norwegian Training - Android")
    }
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}
