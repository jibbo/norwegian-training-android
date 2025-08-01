package com.github.jibbo.norwegiantraining

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private var step: Int = 0
    private var isRunning: Boolean = false
    private var triggerTime: Long = 0L

    private val events: MutableStateFlow<UiCommands> = MutableStateFlow(UiCommands.INITIAL)
    val uiEvents = events.asStateFlow()

    fun scheduleNextAlarm() {
        if (step == 0) {
            triggerTime = getNextAlarmTime()
            events.value = UiCommands.START_ALARM(triggerTime)
            step++
            isRunning = true
        } else {
            isRunning = false
            events.value = UiCommands.STOP_ALARM
        }
    }

    fun permissionGranted(){

    }

    sealed class UiCommands {
        object INITIAL : UiCommands()
        object STOP_ALARM : UiCommands()
        data class START_ALARM(val triggerTime: Long) : UiCommands()
        data class SHOW_NOTIFICATION(val triggerTime: Long) : UiCommands()
    }

    private fun getNextAlarmTime() = System.currentTimeMillis() + when {
        step == 0 -> 10 * 60 // 10 minutes warmup
        else -> 4 * 60 // 4 minutes cardio
    } * 1000
}
