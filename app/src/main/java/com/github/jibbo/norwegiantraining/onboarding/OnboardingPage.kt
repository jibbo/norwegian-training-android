package com.github.jibbo.norwegiantraining.onboarding

import android.Manifest
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.jibbo.norwegiantraining.R

sealed class UiState {
    object Loading : UiState()
    class Show(
        val hasPaid: Boolean,
        val states: List<OnboardingPage>
    ) : UiState()
}

sealed class OnboardingPage(
    @param:StringRes open val title: Int,
    @param:DrawableRes open val image: Int? = null,
) {
    class Normal(
        @param:StringRes override val title: Int,
        @param:StringRes val body: Int,
        @param:StringRes val description: Int,
        @param:DrawableRes override val image: Int? = null,
    ) : OnboardingPage(
        title,
        image
    )

    class Feedback(
        @param:StringRes override val title: Int,
        @param:StringRes val body: Int,
        @param:StringRes val description: Int,
        @param:StringRes val name: Int,
        @param:StringRes val handle: Int,
        @param:DrawableRes override val image: Int? = null,
    ) : OnboardingPage(
        title,
        image
    )

    class Questions(
        @param:StringRes override val title: Int,
        @param:DrawableRes override val image: Int? = null,
        val options: Array<Int>
    ) : OnboardingPage(
        title,
        image
    )

    class Permission(
        @param:StringRes override val title: Int,
        @param:DrawableRes override val image: Int,
        @param:StringRes val body: Int,
        val permission: String
    ) : OnboardingPage(
        title,
        image,
    )
}
