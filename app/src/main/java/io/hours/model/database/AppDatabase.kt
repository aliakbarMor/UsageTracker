package io.hours.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.hours.model.AppCellDao
import io.hours.model.UsageCellDao
import io.hours.model.modules.AppCell
import io.hours.model.modules.PackConverter
import io.hours.model.modules.UsageCell

@Database(entities = [AppCell::class, UsageCell::class], version = 1, exportSchema = false)
@TypeConverters(PackConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val appCellDao: AppCellDao
    abstract val usageCellDao: UsageCellDao

}