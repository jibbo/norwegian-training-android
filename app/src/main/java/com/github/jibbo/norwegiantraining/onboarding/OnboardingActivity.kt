package com.github.jibbo.norwegiantraining.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.SharedPreferencesSettingsRepository
import com.github.jibbo.norwegiantraining.main.MainActivity
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White
import kotlinx.coroutines.launch

class OnboardingActivity : ComponentActivity() {
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
                    Content()
                }
            }
        }
    }
}

@Composable
fun Content() {
    val pagerState = rememberPagerState(pageCount = {
        OnboardingStates.states.size
    })
    Column(
        modifier = Modifier
            .background(
                brush = verticalGradient(
                    colors = listOf(
                        Color.DarkGray,
                        Black
                    )
                )
            )
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { page ->
            OnBoardingPage(
                page,
                pagerState
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) Primary else Color.White
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(RoundedCornerShape(size = 6.dp))
                        .background(color)
                        .width(16.dp)
                        .height(6.dp)
                )
            }
        }
    }
}

@Composable
private fun OnBoardingPage(page: Int, pagerState: PagerState, modifier: Modifier = Modifier) {
    val state = OnboardingStates.states[page]
    Column(
        modifier = modifier
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Text(
            text = state.title.localizable(),
            style = Typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(22.dp))
        when (state) {
            is UiState.Normal -> {
                Text(
                    text = state.description.localizable(),
                    style = Typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                NormalPage(state)
            }

            is UiState.Feedback -> FeedbackPage(state)
            is UiState.Questions -> {
                // TODO move selected inside the questions so that button can answer properly
                val selected = remember { mutableStateOf(0) }
                Questions(state, selected)
            }
        }
        val coroutineScope = rememberCoroutineScope()
        val current = LocalContext.current
        val sessionRepository: SettingsRepository = SharedPreferencesSettingsRepository(current)
        Button(
            onClick = {
                if (page == OnboardingStates.states.size - 1) {
                    sessionRepository.onboardingCompleted()
                    val intent = Intent(current, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    current.startActivity(intent)
                }
                coroutineScope.launch {
                    pagerState.scrollToPage(page + 1)
                }
            }, modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(64.dp)
        ) {
            Text(
                text = R.string.continue_btn.localizable().uppercase(),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ColumnScope.FeedbackPage(state: UiState.Feedback, modifier: Modifier = Modifier) {
    Column(modifier = Modifier.weight(1f)) {
        Spacer(modifier = Modifier.weight(1f))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = modifier.fillMaxWidth(),
        ) {
            if (state.image != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Image(
                        painter = painterResource(
                            id = state.image,
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .width(64.dp)
                            .height(64.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = state.name.localizable(),
                        style = Typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = state.handle.localizable(),
                        style = Typography.bodySmall,
                        color = White.copy(alpha = 0.6f)
                    )
                }

            }
            Text(
                text = state.body.localizable(),
                style = Typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ColumnScope.NormalPage(state: UiState.Normal, modifier: Modifier = Modifier) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.weight(1f))
        if (state.image != null) {
            Image(
                painter = painterResource(
                    id = state.image,
                ),
                contentDescription = null,
                modifier = Modifier.clip(CircleShape)
            )
        }
        Text(
            text = state.body.localizable(),
            style = Typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ColumnScope.Questions(
    state: UiState.Questions,
    selectedOption: MutableState<Int>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        if (state.image != null) {
            Image(
                painter = painterResource(
                    id = state.image,
                ),
                contentDescription = null,
                modifier = Modifier.clip(CircleShape)
            )
        }
        for (option in state.options) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption.value),
                        onClick = { selectedOption.value = option }
                    )
            ) {
                RadioButton(
                    selected = option == selectedOption.value,
                    onClick = null
                )
                Text(
                    text = option.localizable(),
                    style = Typography.bodyLarge,
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NorwegianTrainingTheme {
        Scaffold { _ ->
            Content()
        }
    }
}
