package com.github.jibbo.norwegiantraining.age

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.domain.FitnessLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AgeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val fitnessLevel = settingsRepository.getFitnessLevel()
            val ageRange = getAgeRange(fitnessLevel)

            _uiState.value = AgeUiState(
                fitnessLevel = fitnessLevel,
                ageRange = ageRange
            )
        }
    }

    private fun getAgeRange(fitnessLevel: FitnessLevel): String {
        return when (fitnessLevel) {
            FitnessLevel.BEGINNER -> "20-29 years old"
            FitnessLevel.OCCASIONAL -> "30-39 years old"
            FitnessLevel.FIT -> "40-49 years old"
        }
    }

    data class AgeUiState(
        val fitnessLevel: FitnessLevel,
        val ageRange: String
    ) {
        companion object {
            val Loading = AgeUiState(
                fitnessLevel = FitnessLevel.BEGINNER,
                ageRange = "Loading..."
            )
        }
    }
}