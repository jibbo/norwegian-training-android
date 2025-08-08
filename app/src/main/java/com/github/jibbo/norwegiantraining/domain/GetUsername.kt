package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.UserPreferencesRepo
import javax.inject.Inject

class GetUsername @Inject constructor(
    private val userPreferencesRepo: UserPreferencesRepo
) {
    operator fun invoke(): String = userPreferencesRepo.getUserName() ?: ""
}
