package io.hours.model.modules

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.hours.ui.home.HomeScreenTabs

/**
 * Daily, weekly and monthly cell's in HomeScreen Class
 * shows every single day, week and month
 */
@Entity(tableName = "UsageCell")
data class UsageCell(
    val usageAvatar: String,
    val totalUsage: Long,
    val fromDate: Long,
    val toDate: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var type: UsageType = UsageType.DAY
}

