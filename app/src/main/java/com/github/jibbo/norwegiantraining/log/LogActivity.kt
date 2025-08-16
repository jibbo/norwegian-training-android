package com.github.jibbo.norwegiantraining.log

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.DarkPrimary
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogActivity : BaseActivity() {

    private val viewModel: LogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NorwegianTrainingTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Black,
                                    DarkPrimary
                                )
                            )
                        )
                ) { innerPadding ->
                    val uiState = viewModel.uiState.collectAsState()
                    when (uiState.value) {
                        is UiState.Loading -> {
                            CircularProgressIndicator()
                        }

                        is UiState.Loaded -> {
                            Logs(innerPadding, uiState.value as UiState.Loaded)
                        }
                    }
                }
            }
        }
    }


}
