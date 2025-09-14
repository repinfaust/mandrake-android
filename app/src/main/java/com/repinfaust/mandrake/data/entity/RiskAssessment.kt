package com.repinfaust.mandrake.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class NudgeTier { NONE, SOFT, FIRM }

@Entity(tableName = "risk_assessments")
data class RiskAssessment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = Instant.now().toEpochMilli(),
    val screenerBand: RiskBand,
    val urges7d: Int,
    val highIntensity7d: Int,
    val actedVsAlt14d: Double,
    val nightEpisodes7d: Int,
    val redFlags: RedFlags,
    val nudgeTier: NudgeTier,
    val ruleTriggered: String? = null
)

data class RedFlags(
    val morningUse: Boolean = false,
    val withdrawal: Boolean = false,
    val blackout: Boolean = false,
    val failedCutDown: Boolean = false
)