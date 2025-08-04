package com.github.jibbo.norwegiantraining.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@Dao
interface RecordDao {
    @Query("SELECT * FROM record ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getAll(limit: Int = 10, offset: Int = 0): List<Record>
}

@Entity
@TypeConverters(Converters::class)
data class Record(
    @PrimaryKey val id: Int,
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