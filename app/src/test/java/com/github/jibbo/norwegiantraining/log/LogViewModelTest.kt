package com.github.jibbo.norwegiantraining.log

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.testutils.FakeSessionRepository
import com.github.jibbo.norwegiantraining.testutils.MainDispatcherRule
import java.util.Date
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun groupsSessionsByCalendarMonth() = runTest {
        val repo = FakeSessionRepository()
        repo.insertSessions(
            listOf(
                Session(date = Date(0)),
                Session(date = Date(1))
            )
        )

        val viewModel = LogViewModel(repo)
        advanceUntilIdle()

        val loaded = viewModel.uiState.value as UiState.Loaded
        assertEquals(1, loaded.logs.size)
        assertEquals(2, loaded.logs.values.first()?.size)
    }

    @Test
    fun refreshMonthReplacesOnlyRequestedMonth() = runTest {
        val repo = FakeSessionRepository()
        val september = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 10, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val october = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val oldSeptember = Session(id = 1, date = september)
        val octoberSession = Session(id = 2, date = october)
        repo.insertSessions(listOf(oldSeptember, octoberSession))
        val viewModel = LogViewModel(repo)
        advanceUntilIdle()

        val replacement = Session(id = 3, date = september)
        repo.replaceSessions(listOf(replacement, octoberSession))
        viewModel.refreshMonth(september)
        advanceUntilIdle()

        val loaded = viewModel.uiState.value as UiState.Loaded
        assertEquals(listOf(replacement), loaded.logs[Calendar.SEPTEMBER])
        assertEquals(listOf(octoberSession), loaded.logs[Calendar.OCTOBER])
        assertEquals(1, repo.rangeQueries.size)
        val (from, to) = repo.rangeQueries.single()
        assertEquals(1, Calendar.getInstance().apply { time = from }.get(Calendar.DAY_OF_MONTH))
        assertEquals(30, Calendar.getInstance().apply { time = to }.get(Calendar.DAY_OF_MONTH))
    }
}
