package com.repinfaust.mandrake.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class RiskBand { LOW, ELEVATED, HIGH }
enum class ScreeningType { AUDIT_C, SDS }

@Entity(tableName = "screening_results")
data class ScreeningResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = Instant.now().toEpochMilli(),
    val type: ScreeningType,
    val category: String, // "alcohol", "cannabis", "gambling", etc.
    val responses: List<Int>, // Raw scores for each question
    val band: RiskBand,
    val isSkipped: Boolean = false
)