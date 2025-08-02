package com.github.jibbo.norwegiantraining

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private var currentStep = 0

    private val events: MutableStateFlow<UiCommands> = MutableStateFlow(UiCommands.INITIAL)
    val uiEvents = events.asStateFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiStates = states.asStateFlow()

    fun mainButtonClicked() {
        val oldValue = states.value
        if (oldValue.isTimerRunning) {
            states.value = UiState(oldValue.step, false, oldValue.targetTimeMillis)
            events.value = UiCommands.STOP_ALARM
        } else {
            val targetTimeMillis = if(System.currentTimeMillis() >= oldValue.targetTimeMillis){
                getNextAlarmTime()
            }else{
                oldValue.targetTimeMillis
            }
            states.value = UiState(currentStep, true, targetTimeMillis)
            events.value = UiCommands.START_ALARM(targetTimeMillis)
        }
    }

    fun permissionGranted() {
        val oldValue = states.value
        if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
            events.value = UiCommands.SHOW_NOTIFICATION(oldValue.targetTimeMillis)
        }
    }

    sealed class UiCommands {
        object INITIAL : UiCommands()
        object STOP_ALARM : UiCommands()
        data class START_ALARM(val triggerTime: Long) : UiCommands()
        data class SHOW_NOTIFICATION(val triggerTime: Long) : UiCommands()
    }

    private fun getNextAlarmTime() = System.currentTimeMillis() + when {
        currentStep == 0 -> 10 * 60 // 10 minutes warmup
        else -> 4 * 60 // 4 minutes cardio
    } * 1000

    fun onTimerFinish() {
        currentStep++
        mainButtonClicked()
    }
}
