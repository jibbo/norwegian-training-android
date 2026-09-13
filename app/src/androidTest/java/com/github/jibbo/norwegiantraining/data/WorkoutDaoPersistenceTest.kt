package com.github.jibbo.norwegiantraining.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutDaoPersistenceTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: WorkoutDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.workoutDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun customRowsAreOrderedNewestFirstAndBuiltInsAreFiltered() = runBlocking {
        dao.insert(
            workout(id = 1, name = "Built-in", isCustom = false),
        )
        val olderId = dao.insertCustom(workout(name = "Duplicate"))
        val newerId = dao.insertCustom(workout(name = "Duplicate"))

        assertEquals(listOf(newerId, olderId), dao.getCustomWorkouts().first().map { it.id })
        assertEquals(listOf(1L), dao.getBuiltInWorkouts().first().map { it.id })
        assertEquals(2, dao.getCustomWorkouts().first().size)
    }

    @Test
    fun updatePreservesIdAndClearedIcon() = runBlocking {
        val id = dao.insertCustom(workout(name = "Before", icon = "🔥"))

        val updated = dao.updateCustomById(
            id = id,
            name = "After",
            difficulty = Difficulty.EXPERT,
            content = "5m-2m-1m-5m",
            icon = null,
        )

        assertEquals(1, updated)
        val stored = dao.getById(id)
        assertEquals(id, stored?.id)
        assertEquals("After", stored?.name)
        assertEquals(Difficulty.EXPERT, stored?.difficulty)
        assertEquals("5m-2m-1m-5m", stored?.content)
        assertTrue(stored?.isCustom == true)
        assertEquals(null, stored?.icon)
    }

    @Test
    fun deleteOnlyRemovesCustomRowsAndMissingIdsReturnZero() = runBlocking {
        dao.insert(workout(id = 1, name = "Built-in", isCustom = false))
        val customId = dao.insertCustom(workout(name = "Custom"))

        assertEquals(0, dao.deleteCustomById(999))
        assertEquals(1, dao.deleteCustomById(customId))
        assertEquals(0, dao.deleteCustomById(customId))
        assertEquals(1, dao.getBuiltInWorkouts().first().size)
        assertTrue(dao.getCustomWorkouts().first().isEmpty())
        assertFalse(dao.getById(customId) != null)
    }

    @Test
    fun customFlowReflectsInsertUpdateAndDelete() = runBlocking {
        assertTrue(dao.getCustomWorkouts().first().isEmpty())

        val id = dao.insertCustom(workout(name = "First"))
        assertEquals("First", dao.getCustomWorkouts().first().single().name)

        dao.updateCustomById(id, "Changed", Difficulty.BEGINNER, "5m-1m-1m-5m", "🏃")
        assertEquals("Changed", dao.getCustomWorkouts().first().single().name)
        assertEquals("🏃", dao.getCustomWorkouts().first().single().icon)

        dao.deleteCustomById(id)
        assertTrue(dao.getCustomWorkouts().first().isEmpty())
    }

    private fun workout(
        id: Long = 0,
        name: String,
        isCustom: Boolean = true,
        icon: String? = null,
    ) = Workout(
        id = id,
        name = name,
        difficulty = Difficulty.BEGINNER,
        content = "5m-1m-1m-5m",
        isCustom = isCustom,
        icon = icon,
    )
}
