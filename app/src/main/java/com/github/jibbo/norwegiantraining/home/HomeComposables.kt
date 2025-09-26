package com.github.jibbo.norwegiantraining.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.Toolbar
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme

@Composable
internal fun HomeView(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(horizontal = 16.dp)
    ) {
        Header(viewModel)
        Streak()
    }
}

@Composable
internal fun Header(viewModel: HomeViewModel) {
//        val state by viewModel.uiStates.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.safeDrawingPadding()
    ) {
        Toolbar(
            name = R.string.welcome.localizable("TODO"),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { viewModel.chartsClicked() }) {
            Icon(
                painter = painterResource(R.drawable.outline_area_chart_24),
                contentDescription = ""
            )
        }
        IconButton(onClick = { viewModel.settingsClicked() }) {
            Icon(
                painter = painterResource(R.drawable.baseline_settings_24),
                contentDescription = ""
            )
        }
    }
}

@Composable
internal fun Streak() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Card {

            Text(
                text = "Streak",
            )

        }
    }
}

@Preview
@Composable
fun HomeViewPreview() {
    val settingsRepository = FakeSettingsRepository()
    NorwegianTrainingTheme {
        Scaffold { _ ->
            HomeView(
                HomeViewModel(
                    GetUsername(settingsRepository),
                    settingsRepository
                )
            )
        }
    }
}
