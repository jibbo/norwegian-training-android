package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.log.summarizeDailySessions
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class GetWeeklySessionsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): List<Session?> {
        val calendar = Calendar.getInstance()
        val firstDayOfWeek = calendar.firstDayOfWeek
        
        // Get the start of the current week (locale-specific first day 00:00:00.000)
        calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.time
        
        // Get the end of the current week (day before first day 23:59:59.999)
        calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        calendar.add(Calendar.DAY_OF_MONTH, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.time
        
        val sessions = sessionRepository.getSessionsInRange(startOfWeek, endOfWeek)
        val sessionsByDay = sessions.groupBy { session ->
            Calendar.getInstance().apply { time = session.date }.let {
                it.get(Calendar.YEAR) to it.get(Calendar.DAY_OF_YEAR)
            }
        }
        
        // Return one deliberate summary per day, rather than an arbitrary execution.
        return (0 until 7).map { dayIndex ->
            val dayCalendar = Calendar.getInstance()
            dayCalendar.time = startOfWeek
            dayCalendar.add(Calendar.DAY_OF_MONTH, dayIndex)
            sessionsByDay[
                dayCalendar.get(Calendar.YEAR) to dayCalendar.get(Calendar.DAY_OF_YEAR)
            ].orEmpty().summarizeDailySessions()
        }
    }
}
