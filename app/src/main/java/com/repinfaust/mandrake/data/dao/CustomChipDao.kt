package com.repinfaust.mandrake.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.repinfaust.mandrake.data.entity.CustomChip
import com.repinfaust.mandrake.data.entity.ChipType

@Dao
interface CustomChipDao {
    
    @Query("SELECT * FROM custom_chips WHERE chipType = :chipType AND active = 1 ORDER BY usageCount DESC")
    suspend fun getActiveChipsByType(chipType: ChipType): List<CustomChip>
    
    @Query("SELECT * FROM custom_chips WHERE text = :text AND chipType = :chipType AND active = 1 LIMIT 1")
    suspend fun getChipByTextAndType(text: String, chipType: ChipType): CustomChip?
    
    @Insert
    suspend fun insertChip(chip: CustomChip): Long
    
    @Update
    suspend fun updateChip(chip: CustomChip)
    
    @Query("UPDATE custom_chips SET usageCount = usageCount + 1 WHERE id = :chipId")
    suspend fun incrementUsageCount(chipId: Long)
    
    @Query("UPDATE custom_chips SET active = 0 WHERE id = :chipId")
    suspend fun deactivateChip(chipId: Long)
}