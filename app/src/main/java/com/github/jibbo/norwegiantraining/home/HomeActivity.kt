package com.github.jibbo.norwegiantraining.home

import android.os.Bundle
import androidx.activity.compose.setContent
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
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.components.Toolbar
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                Scaffold { _ ->
                    HomeView()
                }
            }
        }
    }

    @Composable
    private fun HomeView() {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 16.dp)
        ) {
            Header()
            Streak()
        }
    }

    @Composable
    internal fun Header() {
//        val state by viewModel.uiStates.collectAsState()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.safeDrawingPadding()
        ) {
            Toolbar(
                name = R.string.welcome.localizable("TODO"),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { TODO() }) {
                Icon(
                    painter = painterResource(R.drawable.outline_area_chart_24),
                    contentDescription = ""
                )
            }
            IconButton(onClick = { TODO() }) {
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
        NorwegianTrainingTheme {
            Scaffold { _ ->
                HomeView()
            }
        }
    }

}
