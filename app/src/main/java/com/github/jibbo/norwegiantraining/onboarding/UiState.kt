package com.github.jibbo.norwegiantraining.onboarding

import android.Manifest
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.onboarding.OnboardingStates.states

sealed class UiState(
    @param:StringRes open val title: Int,
    @param:DrawableRes open val image: Int? = null,
) {
    class Normal(
        @param:StringRes override val title: Int,
        @param:StringRes val body: Int,
        @param:StringRes val description: Int,
        @param:DrawableRes override val image: Int? = null,
    ) : UiState(
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

    class Permission(
        @param:StringRes override val title: Int,
        @param:DrawableRes override val image: Int,
        @param:StringRes val body: Int,
        val permission: String
    ) : UiState(
        title,
        image,
    )
}


object OnboardingStates {
    private val states = mutableListOf(
//        UiState.Feedback(
//            title = R.string.onboarding_step_1_title,
//            body = R.string.onboarding_step_1_body,
//            description = R.string.onboarding_step_1_description,
//            name = R.string.onboarding_step_1_name,
//            handle = R.string.onboarding_step_1_handle,
//            image = R.mipmap.profile_pic
//        ),
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
        UiState.Normal(
            title = R.string.onboarding_step_2_title,
            description = R.string.onboarding_step_2_description,
            body = R.string.onboarding_step_2_body,
            image = R.drawable.working_out_illustration
        ),

        UiState.Permission(
            title = R.string.onboarding_step_notification_permission_title,
            image = R.drawable.processing_perm_illustration,
            body = R.string.onboarding_step_notification_permission_body,
            permission = Manifest.permission.POST_NOTIFICATIONS
        ),
        UiState.Permission(
            title = R.string.onboarding_step_activity_recognition_permission_title,
            image = R.drawable.physical_perm_illustration,
            body = R.string.onboarding_step_activity_recognition_permission_body,
            permission = Manifest.permission.ACTIVITY_RECOGNITION
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

    fun states() = states
        .filterNot {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                    it is UiState.Permission &&
                    it.permission == Manifest.permission.POST_NOTIFICATIONS
        }
        .filterNot {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    it is UiState.Permission &&
                    it.permission == Manifest.permission.ACTIVITY_RECOGNITION
        }
}
