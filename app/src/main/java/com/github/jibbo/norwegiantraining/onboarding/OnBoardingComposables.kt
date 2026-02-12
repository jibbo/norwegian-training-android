package com.github.jibbo.norwegiantraining.onboarding

import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import androidx.core.content.ContextCompat
import com.github.jibbo.norwegiantraining.BuildConfig
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.components.localizable
import com.github.jibbo.norwegiantraining.data.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.home.HomeActivity
import com.github.jibbo.norwegiantraining.paywall.PaywallActivity
import com.github.jibbo.norwegiantraining.ui.theme.Black
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import com.github.jibbo.norwegiantraining.ui.theme.Primary
import com.github.jibbo.norwegiantraining.ui.theme.Typography
import com.github.jibbo.norwegiantraining.ui.theme.White
import kotlinx.coroutines.launch


@Composable
fun LoadingPage(
    innerPadding: PaddingValues,
    viewModel: OnboardingViewModel
) {
    val state = viewModel.uiStates.collectAsState()
    when (state.value) {
        is UiState.Loading -> Loading(innerPadding)
        is UiState.Show -> OnBoarding(innerPadding, viewModel, state.value as UiState.Show)
    }
}

@Composable
fun Loading(innerPadding: PaddingValues) {
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
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun OnBoarding(innerPadding: PaddingValues, viewModel: OnboardingViewModel, uiState: UiState.Show) {
    val pagerState = rememberPagerState(pageCount = {
        uiState.states.size
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
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .safeDrawingPadding()
                .weight(1f)
                .padding(top = 8.dp),
            userScrollEnabled = true
        ) { page ->
            OnBoardingPage(
                page,
                pagerState,
                uiState.states,
                viewModel,
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
private fun OnBoardingPage(
    page: Int,
    pagerState: PagerState,
    states: List<OnboardingPage>,
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val state = states[page]
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
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
            is OnboardingPage.Normal -> {
                Text(
                    text = state.description.localizable(),
                    style = Typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                NormalPage(state)
            }

            is OnboardingPage.Feedback -> FeedbackPage(state)
            is OnboardingPage.Question -> {
                // TODO move selected inside the questions so that button can answer properly
                val selected = remember { mutableStateOf(1) }
                Questions(page, pagerState, state, selected)
            }

            is OnboardingPage.Permission -> PermissionPage(state)
        }
        Button(
            onClick = {
                viewModel.continueClicked(page)
            }, modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(
                text = R.string.continue_btn.localizable().uppercase(),
                fontWeight = FontWeight.SemiBold,
                color = Black
            )
        }
    }
}

@Composable
fun ColumnScope.FeedbackPage(state: OnboardingPage.Feedback, modifier: Modifier = Modifier) {
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
fun ColumnScope.NormalPage(state: OnboardingPage.Normal, modifier: Modifier = Modifier) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.weight(1f))
        if (state.image != null) {
            Image(
                painter = painterResource(
                    id = state.image,
                ),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(width = 300.dp, height = 300.dp)
            )
        }
        Text(
            text = state.body.localizable(),
            style = Typography.bodyLarge,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ColumnScope.PermissionPage(state: OnboardingPage.Permission, modifier: Modifier = Modifier) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(
                id = state.image,
            ),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .size(width = 300.dp, height = 300.dp)
        )
        Text(
            text = state.body.localizable(),
            style = Typography.bodyLarge,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ColumnScope.Questions(
    page: Int,
    pagerState: PagerState,
    state: OnboardingPage.Question,
    selectedOption: MutableState<Int>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
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
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(page + 1)
                        }
                    }
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
        Scaffold { innerPadding ->
            val settingsRepository = FakeSettingsRepository()
            LoadingPage(
                innerPadding, OnboardingViewModel(
                    settingsRepository
                )
            )
        }
    }
}