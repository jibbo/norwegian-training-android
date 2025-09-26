package com.github.jibbo.norwegiantraining.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Dao
interface SessionDao {
    @Query("SELECT * FROM session ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getAll(limit: Int = 10, offset: Int = 0): List<Session>

    @Upsert
    suspend fun upsert(session: Session): Long

    @Query("SELECT * FROM session where date = strftime('%d-%m-%Y', date())")
    suspend fun getTodaySession(): Session?
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
    fun fromString(value: String?): Date? {
        return value?.let { formatter.parse(it) }
    }

    @TypeConverter
    fun dateToString(date: Date?): String? {
        return date?.let { formatter.format(it) }
    }

    companion object {
        const val format = "dd-MM-yyyy"
        val formatter = SimpleDateFormat(format, Locale.getDefault())
    }
}
