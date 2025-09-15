package com.repinfaust.mandrake.data.repo

import com.repinfaust.mandrake.data.dao.CustomChipDao
import com.repinfaust.mandrake.data.entity.CustomChip
import com.repinfaust.mandrake.data.entity.ChipType
import com.repinfaust.mandrake.domain.model.ActionDomain

class CustomChipRepository(
    private val customChipDao: CustomChipDao
) {
    
    suspend fun getChipsForType(chipType: ChipType): List<CustomChip> {
        return customChipDao.getActiveChipsByType(chipType)
    }
    
    suspend fun createOrIncrementChip(text: String, chipType: ChipType): CustomChip {
        val existingChip = customChipDao.getChipByTextAndType(text, chipType)
        
        return if (existingChip != null) {
            // Increment usage count for existing chip
            customChipDao.incrementUsageCount(existingChip.id)
            existingChip.copy(usageCount = existingChip.usageCount + 1)
        } else {
            // Create new chip
            val newChip = CustomChip(
                text = text,
                chipType = chipType
            )
            val id = customChipDao.insertChip(newChip)
            newChip.copy(id = id)
        }
    }
    
    suspend fun deactivateChip(chipId: Long) {
        customChipDao.deactivateChip(chipId)
    }
    
    suspend fun getAllChipsForType(chipType: ChipType): List<CustomChip> {
        return customChipDao.getChipsByType(chipType)
    }
    
    suspend fun deleteChip(chipId: Long) {
        customChipDao.deleteChip(chipId)
    }
    
    suspend fun createCustomAction(
        text: String, 
        category: ActionDomain, 
        minutes: Int
    ): CustomChip {
        val newChip = CustomChip(
            text = text,
            chipType = ChipType.CUSTOM_ACTION,
            actionCategory = category.name,
            actionMinutes = minutes
        )
        val id = customChipDao.insertChip(newChip)
        return newChip.copy(id = id)
    }
}