package com.github.jibbo.norwegiantraining.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
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

    @Upsert
    suspend fun upsert(session: Session): Long

    @Query("SELECT * FROM session where strftime('%d - %m  - %Y ',date) = strftime('%d - %m  - %Y ', date())")
    suspend fun getTodaySession(): Session?
}

@Entity
@TypeConverters(Converters::class)
data class Session(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "skip_count") val skipCount: Int = 0,
    @ColumnInfo(name = "date") val date: Date = Date()
)

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}
