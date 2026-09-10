package com.github.jibbo.norwegiantraining.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM Workout WHERE difficulty = :difficulty")
    suspend fun getByDifficulty(difficulty: Difficulty): List<Workout>

    @Query("SELECT * FROM Workout WHERE id = :id")
    suspend fun getById(id: Long): Workout?

    @Query("SELECT * FROM Workout")
    fun getAll(): Flow<List<Workout>>

    @Query("SELECT * FROM Workout LIMIT 1")
    fun firstWorkout(): Workout?

    @Query("SELECT DISTINCT difficulty FROM Workout")
    suspend fun getDifficulties(): List<Difficulty>

    @Query("SELECT * FROM Workout WHERE isCustom = 1 ORDER BY id DESC")
    fun getCustomWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM Workout WHERE isCustom = 0 ORDER BY id")
    fun getBuiltInWorkouts(): Flow<List<Workout>>

    @Insert
    suspend fun insertCustom(workout: Workout): Long

    @Query(
        """
        UPDATE Workout
        SET name = :name,
            difficulty = :difficulty,
            content = :content,
            isCustom = 1,
            icon = :icon
        WHERE id = :id AND isCustom = 1
        """
    )
    suspend fun updateCustomById(
        id: Long,
        name: String,
        difficulty: Difficulty,
        content: String,
        icon: String?,
    ): Int

    @Query("DELETE FROM Workout WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomById(id: Long): Int

    @Insert
    suspend fun insert(vararg workout: Workout)
}

@Entity
data class Workout(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "difficulty") val difficulty: Difficulty,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "isCustom") val isCustom: Boolean = false,
    @ColumnInfo(name = "icon") val icon: String? = null,
) {
    @Ignore
    val totalTime = content.split("-").map { return@map it.toSeconds() }.sum().div(60)

    @Ignore
    val totalPhases = content.split("-").size

    @Ignore // total time in minutes / km  * calories per km
    val kCal = (totalTime / 6) * 65

    fun getSplit(): List<Long> = content.split("-").map { it.toMilliSeconds() }

    private fun String.toSeconds(): Int {
        if (last() == 's') {
            return dropLast(1).toInt()
        }
        return dropLast(n = 1).toInt() * 60
    }

    private fun String.toMilliSeconds(): Long {
        if (last() == 's') {
            return dropLast(1).toLong() * 1000
        }
        return dropLast(n = 1).toLong() * 60 * 1000
    }
}

enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    EXPERT,
}

class WorkoutConverters {
    @TypeConverter
    fun toDifficulty(value: Int) = Difficulty.entries[value]

    @TypeConverter
    fun fromDifficulty(value: Difficulty) = value.ordinal
}
