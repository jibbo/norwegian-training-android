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
        if (currentStep > 9 && oldValue.isTimerRunning) { // Timer running during cooldown, user clicks to stop/finish early
            states.value = UiState(currentStep, false, 0L, 0L) // Stop and reset, clear pause time
            events.value = UiCommands.STOP_ALARM
        } else if (currentStep > 9) { // Cooldown finished naturally or skipped, now user clicks to start new cycle
            currentStep = 0
            // No need to set state here, scheduleTimer will do it for the new currentStep
            scheduleTimer() // Start warmup of new cycle
        } else if (oldValue.isTimerRunning) { // Timer is running for a work/warmup step, user clicks to PAUSE
            stopTimer()
        } else { // Timer is not running. Could be paused, or ready for a new interval (e.g. after skip/finish).
                 // scheduleTimer will handle resuming from pause or starting fresh for currentStep.
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

    fun onTimerFinish() { // Called when timer naturally finishes (e.g., by AlarmReceiver) or skipped
        currentStep++
        // currentStep has advanced. The timer is no longer running for the previous step.
        // We need to set a state that reflects this and prepares for the next action.
        // Setting isTimerRunning to false and clearing remainingTimeOnPauseMillis is crucial.
        // mainButtonClicked() will then be called to decide the next step (e.g. schedule next timer or handle end of cycle)
        states.value = UiState(step = currentStep, isTimerRunning = false, targetTimeMillis = 0L, remainingTimeOnPauseMillis = 0L)
        mainButtonClicked() // This will then call scheduleTimer for the new currentStep or handle cycle end
    }

    private fun stopTimer() { // This is effectively "pause"
        val oldValue = states.value
        // Calculate remaining time only if timer was running and target is in future
        val remainingMillis = if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
            oldValue.targetTimeMillis - System.currentTimeMillis()
        } else {
            0L
        }
        // Update state to paused, store remaining time. TargetTimeMillis can be kept for reference or cleared.
        states.value = UiState(oldValue.step, false, oldValue.targetTimeMillis, remainingMillis)
        events.value = UiCommands.STOP_ALARM // To stop actual alarm/notification
    }

    private fun scheduleTimer() { // This handles RESUME or STARTING a new interval
        val oldValue = states.value
        val newTargetTimeMillis: Long

        // Check if we are RESUMING from pause for the *same current step*
        if (!oldValue.isTimerRunning && oldValue.remainingTimeOnPauseMillis > 0L && oldValue.step == currentStep) {
            newTargetTimeMillis = System.currentTimeMillis() + oldValue.remainingTimeOnPauseMillis
            // Update state: timer is running, new target time, clear remaining pause time
            states.value = UiState(currentStep, true, newTargetTimeMillis, 0L)
        } else {
            // STARTING a new timer interval (e.g. fresh start, step advanced, or no valid pause time)
            // getNextAlarmTime() calculates System.currentTimeMillis() + duration for the currentStep
            newTargetTimeMillis = getNextAlarmTime()
            // Update state: timer is running, new target time, clear remaining pause time
            states.value = UiState(currentStep, true, newTargetTimeMillis, 0L)
        }
        events.value = UiCommands.START_ALARM(newTargetTimeMillis) // To start actual alarm/notification
    }

    // getNextAlarmTime now returns the absolute target time
    private fun getNextAlarmTime(): Long {
        val durationMillis = when (currentStep) {
            0 -> 10 * 60 * 1000 // 10 minutes warmup
            9 -> 5 * 60 * 1000  // 5 minutes cooldown
            else -> 4 * 60 * 1000 // 4 minutes work
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
