package com.github.jibbo.norwegiantraining.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.jibbo.norwegiantraining.R

data class UiState(
    val layout: PageLayout,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    @param:StringRes val body: Int? = null,
    @param:DrawableRes val image: Int? = null,
)

enum class PageLayout {
    NORMAL,
    FEEDBACK
}

object OnboardingStates {
    val states = listOf(
        UiState(
            layout = PageLayout.FEEDBACK,
            title = R.string.onboarding_step_1_title,
            description = R.string.onboarding_step_1_description,
            body = R.string.onboarding_step_1_body,
            image = R.mipmap.profile_pic
        ),
        UiState(
            layout = PageLayout.NORMAL,
            title = R.string.onboarding_step_1_title,
            description = R.string.onboarding_step_1_description,
            body = R.string.onboarding_step_1_body,
            image = R.mipmap.profile_pic
        ),
    )
}
