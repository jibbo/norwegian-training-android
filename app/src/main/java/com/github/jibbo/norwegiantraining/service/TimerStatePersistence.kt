package com.github.jibbo.norwegiantraining.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.jibbo.norwegiantraining.domain.Phase
import com.github.jibbo.norwegiantraining.domain.PhaseName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.timerDataStore: DataStore<Preferences> by preferencesDataStore(name = "workout_timer_state")

data class WorkoutTimerState(
    val workoutId: Long = -1L,
    val workoutName: String = "",
    val currentPhaseIndex: Int = 0,
    val currentPhase: Phase = Phase(PhaseName.GET_READY, 0L),
    val targetTimeMillis: Long = 0L,
    val isTimerRunning: Boolean = false,
    val remainingTimeOnPauseMillis: Long = 0L,
    val isCompleted: Boolean = false
)

@Singleton
class TimerStatePersistence @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.timerDataStore

    private object Keys {
        val WORKOUT_ID = longPreferencesKey("workout_id")
        val WORKOUT_NAME = stringPreferencesKey("workout_name")
        val CURRENT_PHASE_INDEX = intPreferencesKey("current_phase_index")
        val CURRENT_PHASE_NAME = stringPreferencesKey("current_phase_name")
        val CURRENT_PHASE_DURATION = longPreferencesKey("current_phase_duration")
        val TARGET_TIME_MILLIS = longPreferencesKey("target_time_millis")
        val IS_TIMER_RUNNING = booleanPreferencesKey("is_timer_running")
        val REMAINING_TIME_ON_PAUSE = longPreferencesKey("remaining_time_on_pause")
        val IS_COMPLETED = booleanPreferencesKey("is_completed")
    }

    suspend fun saveState(state: WorkoutTimerState) {
        dataStore.edit { preferences ->
            preferences[Keys.WORKOUT_ID] = state.workoutId
            preferences[Keys.WORKOUT_NAME] = state.workoutName
            preferences[Keys.CURRENT_PHASE_INDEX] = state.currentPhaseIndex
            preferences[Keys.CURRENT_PHASE_NAME] = state.currentPhase.name.name
            preferences[Keys.CURRENT_PHASE_DURATION] = state.currentPhase.durationMillis
            preferences[Keys.TARGET_TIME_MILLIS] = state.targetTimeMillis
            preferences[Keys.IS_TIMER_RUNNING] = state.isTimerRunning
            preferences[Keys.REMAINING_TIME_ON_PAUSE] = state.remainingTimeOnPauseMillis
            preferences[Keys.IS_COMPLETED] = state.isCompleted
        }
    }

    suspend fun loadState(): WorkoutTimerState? {
        val preferences = dataStore.data.first()
        val workoutId = preferences[Keys.WORKOUT_ID] ?: return null

        if (workoutId == -1L) return null

        val phaseName = preferences[Keys.CURRENT_PHASE_NAME]?.let {
            PhaseName.valueOf(it)
        } ?: PhaseName.GET_READY

        val phaseDuration = preferences[Keys.CURRENT_PHASE_DURATION] ?: 0L

        return WorkoutTimerState(
            workoutId = workoutId,
            workoutName = preferences[Keys.WORKOUT_NAME] ?: "",
            currentPhaseIndex = preferences[Keys.CURRENT_PHASE_INDEX] ?: 0,
            currentPhase = Phase(phaseName, phaseDuration),
            targetTimeMillis = preferences[Keys.TARGET_TIME_MILLIS] ?: 0L,
            isTimerRunning = preferences[Keys.IS_TIMER_RUNNING] ?: false,
            remainingTimeOnPauseMillis = preferences[Keys.REMAINING_TIME_ON_PAUSE] ?: 0L,
            isCompleted = preferences[Keys.IS_COMPLETED] ?: false
        )
    }

    suspend fun clearState() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
