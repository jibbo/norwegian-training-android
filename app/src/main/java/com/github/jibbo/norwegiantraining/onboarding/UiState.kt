package com.github.jibbo.norwegiantraining.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.jibbo.norwegiantraining.R

sealed class UiState(
    @param:StringRes open val title: Int,
    @param:DrawableRes open val image: Int? = null,
) {
    class Normal(
        @param:StringRes override val title: Int,
        @param:StringRes val description: Int,
        @param:StringRes val body: Int,
        @param:DrawableRes override val image: Int? = null,
    ) : UiState(
        title,
        image
    )

    class Feedback(
        @param:StringRes override val title: Int,
        @param:StringRes val description: Int,
        @param:StringRes val name: Int,
        @param:StringRes val handle: Int,
        @param:StringRes val body: Int,
        @param:DrawableRes override val image: Int? = null,
    ) : UiState(
        title,
        image
    )

    class Questions(
        @param:StringRes override val title: Int,
        @param:DrawableRes override val image: Int? = null,
        val options: Array<Int>
    ) : UiState(
        title,
        image
    )
}


object OnboardingStates {
    val states = listOf(
//        UiState.Feedback(
//            title = R.string.onboarding_step_1_title,
//            description = R.string.onboarding_step_1_description,
//            name = R.string.onboarding_step_1_name,
//            handle = R.string.onboarding_step_1_handle,
//            body = R.string.onboarding_step_1_body,
//            image = R.mipmap.profile_pic
//        ),
        UiState.Normal(
            title = R.string.onboarding_step_2_title,
            description = R.string.onboarding_step_2_description,
            body = R.string.onboarding_step_2_body,
            image = R.drawable.working_out_illustration
        ),
        UiState.Normal(
            title = R.string.onboarding_step_6_title,
            description = R.string.onboarding_step_6_description,
            body = R.string.onboarding_step_6_body,
            image = R.drawable.runner_illustration
        ),
        UiState.Normal(
            title = R.string.onboarding_step_5_title,
            description = R.string.onboarding_step_5_description,
            body = R.string.onboarding_step_5_body,
            image = R.drawable.organizing_data_illustration
        ),
//        UiState.Questions(
//            title = R.string.onboarding_step_3_title,
//            options = arrayOf(
//                R.string.onboarding_step_3_option_1,
//                R.string.onboarding_step_3_option_2
//            )
//        ),
        UiState.Normal(
            title = R.string.onboarding_step_4_title,
            description = R.string.onboarding_step_4_description,
            body = R.string.onboarding_step_4_body,
            image = R.drawable.hearth_illustration
        ),
    )
}
