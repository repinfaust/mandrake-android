package com.repinfaust.mandrake.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class ChipType { TACTIC, URGE_ABOUT, CUSTOM_ACTION }

@Entity(tableName = "custom_chips")
data class CustomChip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val chipType: ChipType,
    val usageCount: Int = 1,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val active: Boolean = true,
    // Additional fields for custom actions
    val actionCategory: String? = null, // ActionDomain name for custom actions
    val actionMinutes: Int? = null // Duration for custom actions
)