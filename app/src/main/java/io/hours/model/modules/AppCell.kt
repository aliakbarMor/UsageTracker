package io.hours.model.modules

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter


@Entity(tableName = "AppCell")
data class AppCell(
    var pack: Package,
    var usageTime: Long,
    var visit: Int,
) {
    var usageCellId: Long = 0

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

}

class PackConverter {
    @TypeConverter
    fun packToString(pack: Package): String {
        return pack.packageName
    }

    @TypeConverter
    fun stringToPack(string: String): Package {
        return Package(string)
    }
}