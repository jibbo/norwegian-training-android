package com.github.jibbo.norwegiantraining

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.compareTo

class MainViewModel : ViewModel() {
    private val events: MutableStateFlow<UiCommands> = MutableStateFlow(UiCommands.INITIAL)
    val uiEvents = events.asStateFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiStates = states.asStateFlow()

    fun scheduleNextAlarm() {
        val oldValue = states.value
        if(oldValue.step == 9){
            TODO("completed workout ui")
        }
        if(oldValue.isTimerRunning){
            states.value = UiState(oldValue.step, false, oldValue.targetTimeMillis)
            events.value = UiCommands.STOP_ALARM
        } else{
            val targetTimeMillis = getNextAlarmTime()
            states.value = UiState(oldValue.step + 1, true, targetTimeMillis)
            events.value = UiCommands.START_ALARM(targetTimeMillis)
        }
    }

    fun permissionGranted(){
        val oldValue = states.value
        if(oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()){
            events.value = UiCommands.SHOW_NOTIFICATION(oldValue.targetTimeMillis)
        }
    }

    sealed class UiCommands {
        object INITIAL : UiCommands()
        object STOP_ALARM : UiCommands()
        data class START_ALARM(val triggerTime: Long) : UiCommands()
        data class SHOW_NOTIFICATION(val triggerTime: Long) : UiCommands()
    }

    data class UiState(val step: Int = 0, val isTimerRunning: Boolean = false, val targetTimeMillis: Long = 0)

    private fun getNextAlarmTime() = System.currentTimeMillis() + when {
        states.value.step == 0 -> 10 * 60 // 10 minutes warmup
        else -> 4 * 60 // 4 minutes cardio
    } * 1000

    fun onTimerFinish() {
        scheduleNextAlarm()
    }
}
