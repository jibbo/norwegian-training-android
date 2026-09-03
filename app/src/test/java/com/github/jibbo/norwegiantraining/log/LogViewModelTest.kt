package com.github.jibbo.norwegiantraining.log

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.testutils.FakeSessionRepository
import com.github.jibbo.norwegiantraining.testutils.MainDispatcherRule
import java.util.Date
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
}
