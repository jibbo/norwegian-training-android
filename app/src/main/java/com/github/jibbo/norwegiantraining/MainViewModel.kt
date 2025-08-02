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
        if(currentStep > 9 && oldValue.isTimerRunning){
            states.value = UiState(currentStep, false, 0L)
            events.value = UiCommands.STOP_ALARM
        }
        else if(currentStep > 9){
            currentStep = 0
            scheduleTimer()
        }
        else if(currentStep > oldValue.step){
            scheduleTimer()
        }
        else if (oldValue.isTimerRunning) {
            stopTimer()
        } else {
            // pause
            scheduleTimer()
        }
    }

    fun showSkipButton() = currentStep>= 0 && currentStep < 10

    fun showCountdown() = currentStep >= 0 && currentStep < 10

    fun permissionGranted() {
        val oldValue = states.value
        if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
            events.value = UiCommands.SHOW_NOTIFICATION(oldValue.targetTimeMillis)
        }
    }

    fun skipClicked(){
        onTimerFinish()
    }

    fun onTimerFinish() {
        currentStep++
        mainButtonClicked()
    }

    private fun stopTimer() {
        val oldValue = states.value
        states.value = UiState(oldValue.step, false, oldValue.targetTimeMillis)
        events.value = UiCommands.STOP_ALARM
    }

    private fun scheduleTimer() {
        val oldValue = states.value
        val targetTimeMillis = if (System.currentTimeMillis() >= oldValue.targetTimeMillis || currentStep > oldValue.step) {
            getNextAlarmTime()
        } else {
            oldValue.targetTimeMillis
        }
        states.value = UiState(currentStep, true, targetTimeMillis)
        events.value = UiCommands.START_ALARM(targetTimeMillis)
    }

    private fun getNextAlarmTime() = System.currentTimeMillis() + when {
        currentStep == 0 -> 10 * 60 // 10 minutes warmup
        currentStep == 9 -> 5 * 60 // 5 minutes cooldown
        else -> 4 * 60 // 4 minutes
    } * 1000

    sealed class UiCommands {
        object INITIAL : UiCommands()
        object STOP_ALARM : UiCommands()
        data class START_ALARM(val triggerTime: Long) : UiCommands()
        data class SHOW_NOTIFICATION(val triggerTime: Long) : UiCommands()
    }
}
