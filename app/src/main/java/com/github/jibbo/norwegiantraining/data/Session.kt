package com.github.jibbo.norwegiantraining.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import java.util.Date

@Dao
interface SessionDao {
    @Query("SELECT * FROM session ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getAll(limit: Int = 10, offset: Int = 0): List<Session>

    @Query("SELECT * FROM session WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    suspend fun getInRange(from: Date, to: Date): List<Session>

    @Upsert
    suspend fun upsert(session: Session): Long

    @Insert
    suspend fun insert(session: Session): Long

    @Insert
    suspend fun insertManual(session: Session): Long

    @Insert
    suspend fun insert(sessions: List<Session>)

    @Query("SELECT * FROM session WHERE is_manual = 0 AND date BETWEEN :startOfDay AND :endOfDay ORDER BY date DESC LIMIT 1")
    suspend fun getTodaySession(startOfDay: Long, endOfDay: Long): Session?

    @Query("SELECT * FROM session WHERE id = :id")
    suspend fun getById(id: Long): Session?

    @Query("DELETE FROM session WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM session WHERE workout_id = :workoutId AND is_manual = 0 AND date BETWEEN :from AND :to ORDER BY date DESC LIMIT 1")
    suspend fun getNormalForWorkoutInRange(workoutId: Long, from: Long, to: Long): Session?

    @Query("SELECT * FROM session WHERE workout_id IS NULL AND is_manual = 0 AND name = :name AND duration = :duration AND date BETWEEN :from AND :to ORDER BY date DESC LIMIT 1")
    suspend fun getLegacyNormalInRange(name: String, duration: Long, from: Long, to: Long): Session?

    @Query("UPDATE session SET phases_ended = phases_ended + 1 WHERE id = :id")
    suspend fun incrementPhasesEnded(id: Long): Int

    @Query("UPDATE session SET skip_count = skip_count + 1 WHERE id = :id")
    suspend fun incrementSkipCount(id: Long): Int
}

@Entity
@TypeConverters(SessionConverters::class)
data class Session(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "phases_ended") val phasesEnded: Int = 0,
    @ColumnInfo(name = "skip_count") val skipCount: Int = 0,
    @ColumnInfo(name = "date") val date: Date = Date(),
    @ColumnInfo(name = "is_manual") val isManual: Boolean = false,
    @ColumnInfo(name = "name") val name: String = "HIIT",
    @ColumnInfo(name = "duration") val duration: Long = 0L,
    @ColumnInfo(name = "activity_type") val activityType: String = "HIIT",
    @ColumnInfo(name = "workout_id") val workoutId: Long? = null,
)

class SessionConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
