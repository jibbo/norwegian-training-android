package com.github.jibbo.norwegiantraining.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM Workout WHERE difficulty = :difficulty")
    suspend fun getByDifficulty(difficulty: Difficulty): List<Workout>

    @Query("SELECT * FROM Workout WHERE id = :id")
    suspend fun getById(id: Long): Workout?

    @Query("SELECT * FROM Workout")
    fun getAll(): Flow<List<Workout>>

    @Query("SELECT * FROM Workout")
    fun syncGetAll(): List<Workout>

    @Query("SELECT DISTINCT difficulty FROM Workout")
    suspend fun getDifficulties(): List<Difficulty>

    @Insert
    suspend fun insert(vararg workout: Workout)
}

@Entity
@TypeConverters(WorkoutConverters::class)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "difficulty") val difficulty: Difficulty,
    @ColumnInfo(name = "content") val content: String,
) {
    @Ignore
    val totalTime = content.split("-").map { return@map it.toSeconds() }.sum().div(60)

    fun restTime(): Int {
        val tmp = content.split("-")
        return (tmp.first().toSeconds() + tmp.last().toSeconds()).div(60)
    }

    fun getSplit(): List<Long> = content.split("-").map { it.toMilliSeconds() }

    fun splitText(withWarmup: Boolean = true, withCooldown: Boolean = true): Int {
        val split = getSplit()
        val splitSize = if (!withWarmup && !withCooldown) {
            split.removeFromSize(2)
        } else if (!withWarmup || !withCooldown) {
            split.removeFromSize(1)
        } else {
            split.size
        }
        return splitSize / 2
    }

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

    private fun List<Any>.removeFromSize(howMany: Int) = if (size > howMany) {
        size - howMany
    } else {
        0
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
