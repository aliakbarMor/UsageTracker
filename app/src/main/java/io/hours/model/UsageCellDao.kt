package io.hours.model

import androidx.room.*
import io.hours.model.modules.AppCell
import io.hours.model.modules.UsageCell
import io.hours.model.modules.UsageType

@Dao
interface UsageCellDao {

    @Query("SELECT * FROM UsageCell WHERE id =:usageCellId")
    suspend fun getWithId(usageCellId: Long): UsageCell

    @Query("SELECT * FROM UsageCell WHERE type = :type and fromDate BETWEEN :fromDate AND :toDate")
    suspend fun getWithDate(
        fromDate: Long, toDate: Long, type: UsageType = UsageType.DAY
    ): List<UsageCell>

    @Query("SELECT * FROM UsageCell WHERE type = :type")
    suspend fun getAll(type: UsageType): List<UsageCell>

    @Query("SELECT EXISTS (SELECT id FROM UsageCell WHERE type = :type and fromDate BETWEEN :startDate AND :endDate)")
    suspend fun exist(startDate: Long, endDate: Long, type: UsageType): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(usageCell: UsageCell): Long

    @Insert
    suspend fun addAll(list: List<UsageCell>)

}