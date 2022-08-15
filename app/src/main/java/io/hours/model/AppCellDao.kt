package io.hours.model

import androidx.room.*
import io.hours.model.modules.AppCell

@Dao
interface AppCellDao {

    @Query("SELECT * FROM AppCell WHERE usageCellId = :idUsageCell order by usageTime desc")
    suspend fun getAll(idUsageCell: Long): List<AppCell>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(appCell: AppCell)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAll(list: List<AppCell>)

}