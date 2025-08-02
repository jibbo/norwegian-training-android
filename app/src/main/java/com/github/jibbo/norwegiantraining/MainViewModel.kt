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
        if (currentStep > 9 && oldValue.isTimerRunning) { 
            states.value = UiState(currentStep, false, 0L, 0L) 
            events.value = UiCommands.STOP_ALARM
        } else if (currentStep > 9) { 
            currentStep = 0
            
            scheduleTimer() 
        } else if (oldValue.isTimerRunning) { 
            stopTimer()
        } else { 
            
            scheduleTimer()
        }
    }

    fun showSkipButton() = currentStep >= 0 && currentStep < 10

    fun showCountdown() = currentStep >= 0 && currentStep < 10

    fun permissionGranted() {
        val oldValue = states.value
        if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
            events.value = UiCommands.SHOW_NOTIFICATION(oldValue.targetTimeMillis)
        }
    }

    fun skipClicked() {
        onTimerFinish()
    }

    fun onTimerFinish() { 
        currentStep++
        
        
        
        states.value = UiState(step = currentStep, isTimerRunning = false, targetTimeMillis = 0L, remainingTimeOnPauseMillis = 0L)
        mainButtonClicked() 
    }

    private fun stopTimer() { 
        val oldValue = states.value
        
        val remainingMillis = if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
            oldValue.targetTimeMillis - System.currentTimeMillis()
        } else {
            0L
        }
        
        states.value = UiState(oldValue.step, false, oldValue.targetTimeMillis, remainingMillis)
        events.value = UiCommands.STOP_ALARM 
    }

    private fun scheduleTimer() {
        val oldValue = states.value
        val newTargetTimeMillis: Long
        
        if (!oldValue.isTimerRunning && oldValue.remainingTimeOnPauseMillis > 0L && oldValue.step == currentStep) {
            newTargetTimeMillis = System.currentTimeMillis() + oldValue.remainingTimeOnPauseMillis
            states.value = UiState(currentStep, true, newTargetTimeMillis, 0L)
        } else {
            newTargetTimeMillis = getNextAlarmTime()
            states.value = UiState(currentStep, true, newTargetTimeMillis, 0L)
        }
        events.value = UiCommands.START_ALARM(newTargetTimeMillis) 
    }

    
    private fun getNextAlarmTime(): Long {
        val durationMillis = when (currentStep) {
            0 -> 10 * 60 * 1000 
            9 -> 5 * 60 * 1000  
            else -> 4 * 60 * 1000 
        }
        return System.currentTimeMillis() + durationMillis
    }

    sealed class UiCommands {
        object INITIAL : UiCommands()
        object STOP_ALARM : UiCommands()
        data class START_ALARM(val triggerTime: Long) : UiCommands()
        data class SHOW_NOTIFICATION(val triggerTime: Long) : UiCommands()
    }
}
