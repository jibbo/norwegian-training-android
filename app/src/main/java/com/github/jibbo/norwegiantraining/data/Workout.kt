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
    fun getByDifficulty(difficulty: Int): List<Workout>

    @Query("SELECT DISTINCT difficulty FROM Workout")
    fun getDifficulties(): List<Difficulty>
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
