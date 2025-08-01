package com.github.jibbo.norwegiantraining

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private var step: Int = 0
    private var isRunning: Boolean = false

    private val events: MutableStateFlow<UiCommands> = MutableStateFlow(UiCommands.INITIAL)
    val uiEvents = events.asStateFlow()

    fun scheduleNextAlarm() {
        if (step == 0) {
            events.value = UiCommands.START_ALARM(getNextAlarmTime())
            step++
            isRunning = true
        } else {
            isRunning = false
            events.value = UiCommands.STOP_ALARM
        }
    }

    sealed class UiCommands {
        object INITIAL : UiCommands()
        object STOP_ALARM : UiCommands()
        data class START_ALARM(val seconds: Int) : UiCommands()
    }

    private fun getNextAlarmTime() = when {
        step == 0 -> 10 * 60 // 10 minutes warmup
        else -> 4 * 60 // 4 minutes cardio
    }
}
