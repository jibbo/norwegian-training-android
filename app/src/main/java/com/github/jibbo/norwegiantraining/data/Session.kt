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
    suspend fun insert(sessions: List<Session>)

    @Query("SELECT * FROM session WHERE date BETWEEN :startOfDay AND :endOfDay LIMIT 1")
    suspend fun getTodaySession(startOfDay: Long, endOfDay: Long): Session?
}

@Entity
@TypeConverters(SessionConverters::class)
data class Session(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "phases_ended") val phasesEnded: Int = 0,
    @ColumnInfo(name = "skip_count") val skipCount: Int = 0,
    @ColumnInfo(name = "date") val date: Date = Date()
)

class SessionConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
