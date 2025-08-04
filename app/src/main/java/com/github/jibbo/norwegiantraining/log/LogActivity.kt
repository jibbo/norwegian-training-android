package com.github.jibbo.norwegiantraining.log

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@AndroidEntryPoint
class LogActivity : AppCompatActivity() {

    private val viewModel: LogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NorwegianTrainingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
