package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.log.SessionStatus
import com.github.jibbo.norwegiantraining.testutils.FakeSessionRepository
import com.github.jibbo.norwegiantraining.testutils.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.testutils.FakeWorkoutRepository
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCaseTests {
    @Test
    fun getTodaySessionInsertsAndReturnsGeneratedCopy() = runTest {
        val sessions = FakeSessionRepository()
        val useCase = GetTodaySessionUseCase(sessions)

        val result = useCase()

        assertEquals(0L, result.id)
        assertEquals(result, sessions.getTodaySession())
    }

    @Test
    fun getAllWorkoutsGroupsByDifficulty() = runTest {
        val workouts = FakeWorkoutRepository()
        workouts.insert(
            Workout(1, "A", Difficulty.BEGINNER, "10s-20s"),
            Workout(2, "B", Difficulty.INTERMEDIATE, "10s-20s")
        )
        val useCase = GetAllWorkouts(workouts)

        val grouped = useCase().first()

        assertEquals(listOf(1L), grouped[Difficulty.BEGINNER]?.map { it.id })
        assertEquals(listOf(2L), grouped[Difficulty.INTERMEDIATE]?.map { it.id })
    }

    @Test
    fun getRecommendedWorkoutIdPrefersValidStoredRecommendation() {
        val settings = FakeSettingsRepository().apply {
            setRecommendedWorkoutId(2L)
            setFitnessLevel(FitnessLevel.BEGINNER)
        }
        val workouts = mapOf(
            Difficulty.BEGINNER to listOf(
                Workout(1, "A", Difficulty.BEGINNER, "10s-20s"),
                Workout(2, "B", Difficulty.BEGINNER, "10s-20s")
            )
        )

        val result = GetRecommendedWorkoutId(settings)(workouts)

        assertEquals(2L, result)
    }

    @Test
    fun getRecommendedWorkoutIdFallsBackToFitnessLevel() {
        val settings = FakeSettingsRepository().apply {
            setFitnessLevel(FitnessLevel.OCCASIONAL)
        }
        val workouts = mapOf(
            Difficulty.BEGINNER to listOf(Workout(1, "A", Difficulty.BEGINNER, "10s-20s")),
            Difficulty.INTERMEDIATE to listOf(Workout(2, "B", Difficulty.INTERMEDIATE, "10s-20s"))
        )

        val result = GetRecommendedWorkoutId(settings)(workouts)

        assertEquals(2L, result)
    }

    @Test
    fun getWeeklySessionsReturnsSevenSlots() = runTest {
        val sessions = FakeSessionRepository()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time
        sessions.insertSessions(
            listOf(
                Session(date = start),
                Session(date = Date(start.time + 24L * 60 * 60 * 1000))
            )
        )
        val useCase = GetWeeklySessionsUseCase(sessions)

        val result = useCase()

        assertEquals(7, result.size)
        assertTrue(result[0] != null)
    }

    @Test
    fun freeTrialAndOnboardingAndUsernameUseCasesExposeRepositoryValues() {
        val settings = FakeSettingsRepository().apply {
            setUserName("Didi")
            onboardingCompleted()
            debugOnlySetFreeTrialDate(Date(System.currentTimeMillis() + 60_000))
        }

        assertEquals("Didi", GetUsername(settings)())
        assertTrue(IsOnboardingCompleted(settings)())
        assertTrue(IsFreeTrial(settings)())
        settings.debugOnlySetFreeTrialDate(Date(System.currentTimeMillis() - 60_000))
        assertFalse(IsFreeTrial(settings)())
    }

    @Test
    fun workoutCompletedUseCaseRecordsLastWorkoutOnlyWhenSessionIsNotBad() = runTest {
        val sessions = FakeSessionRepository()
        val settings = FakeSettingsRepository()
        val workouts = FakeWorkoutRepository()
        workouts.insert(Workout(1, "A", Difficulty.BEGINNER, "10s-20s"))
        val today = Session(phasesEnded = 2, skipCount = 0, date = Date())
        sessions.insertSession(today)
        val checkProgression = ApplyProgressionUseCase(sessions, workouts, settings)
        val useCase = WorkoutCompletedUseCase(GetTodaySessionUseCase(sessions), sessions, settings, checkProgression)

        val result = useCase(1L)

        assertEquals(3, result.session.phasesEnded)
        assertEquals(null, settings.getLastWorkoutId())
    }

    @Test
    fun applyProgressionAdvancesWorkoutAfterEnoughGoodWeeks() = runTest {
        val sessions = FakeSessionRepository()
        val settings = FakeSettingsRepository().apply {
            setFitnessLevel(FitnessLevel.BEGINNER)
            setRecommendedWorkoutId(1L)
        }
        val workouts = FakeWorkoutRepository()
        workouts.insert(
            Workout(1, "A", Difficulty.BEGINNER, "10s-20s"),
            Workout(2, "B", Difficulty.BEGINNER, "10s-20s")
        )
        val now = Calendar.getInstance()
        repeat(4) { week ->
            repeat(3) { day ->
                sessions.insertSession(
                    Session(
                        phasesEnded = 8,
                        skipCount = 0,
                        date = Date(now.timeInMillis - ((week * 7 + day + 1).toLong() * 24 * 60 * 60 * 1000))
                    )
                )
            }
        }
        val useCase = ApplyProgressionUseCase(sessions, workouts, settings)

        val result = useCase(1L, Session(phasesEnded = 8, skipCount = 0, date = Date()))

        assertEquals(ProgressionResult.NextWorkout::class, result::class)
        assertEquals(2L, settings.getRecommendedWorkoutId())
    }

    @Test
    fun applyProgressionDoesNothingWhenWeeksAreInsufficient() = runTest {
        val sessions = FakeSessionRepository()
        val settings = FakeSettingsRepository().apply {
            setFitnessLevel(FitnessLevel.BEGINNER)
            setRecommendedWorkoutId(1L)
        }
        val workouts = FakeWorkoutRepository()
        workouts.insert(Workout(1, "A", Difficulty.BEGINNER, "10s-20s"))
        val useCase = ApplyProgressionUseCase(sessions, workouts, settings)

        val result = useCase(1L, Session(phasesEnded = 1, skipCount = 0, date = Date()))

        assertEquals(ProgressionResult.NoChange::class, result::class)
    }
}
