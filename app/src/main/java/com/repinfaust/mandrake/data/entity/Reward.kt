package com.repinfaust.mandrake.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String? = null,
    val costPoints: Int? = null, // nullable for milestone-only unlocks
    val template: Boolean = false, // true for provided templates
    val active: Boolean = true,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val requiredMilestoneCount: Int = 1, // How many milestones needed to unlock
    val specificMilestoneId: String? = null, // Optional specific milestone requirement
    val milestoneType: MilestoneType? = null // Optional milestone type filter
)

@Entity(tableName = "redemptions")
data class Redemption(
    @PrimaryKey val id: String,
    val rewardId: String,
    val eventRef: String, // milestoneId or urgeEventId
    val timestamp: Long = Instant.now().toEpochMilli(),
    val claimSource: String // "milestone" or "urge"
)

data class UserPreferences(
    val usePoints: Boolean = true // points mode vs milestone mode
)