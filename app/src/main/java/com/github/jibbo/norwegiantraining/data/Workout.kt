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

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM Workout WHERE difficulty = :difficulty")
    suspend fun getByDifficulty(difficulty: Difficulty): List<Workout>

    @Query("SELECT * FROM Workout WHERE id = :id")
    suspend fun getById(id: Long): Workout?

    @Query("SELECT * FROM Workout")
    suspend fun getAll(): List<Workout>

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
    val totalTime = content.split("-").map { return@map it.toTime() }.sum().div(60)

    fun restTime(): Int {
        val tmp = content.split("-")
        return tmp.first().toTime() + tmp.last().toTime()
    }

    fun splitText(): String {
        val tmp = content.split("-")
        val list = tmp.subList(1, tmp.size - 1)
        val split = list.size / 2
        return "$split x $split"
    }

    private fun String.toTime(): Int {
        if (last() == 's') {
            return dropLast(1).toInt()
        }
        return dropLast(n = 1).toInt() * 60
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
