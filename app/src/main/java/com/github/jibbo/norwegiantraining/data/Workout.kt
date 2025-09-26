package com.github.jibbo.norwegiantraining.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
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
}

@Entity
@TypeConverters(WorkoutConverters::class)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "difficulty") val difficulty: Difficulty,
    @ColumnInfo(name = "content") val content: String,
)

enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    EXPERT,
}

class WorkoutConverters {
    @TypeConverter
    fun toDifficulty(value: Int) = enumValues<Difficulty>()[value]

    @TypeConverter
    fun fromDifficulty(value: Difficulty) = value.ordinal
}
