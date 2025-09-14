package com.repinfaust.mandrake.data.repo

import com.repinfaust.mandrake.data.dao.RewardsDao
import com.repinfaust.mandrake.data.dao.RedemptionsDao
import com.repinfaust.mandrake.data.entity.Reward
import com.repinfaust.mandrake.data.entity.Redemption
import com.repinfaust.mandrake.data.entity.MilestoneType
import java.util.UUID

class RewardsRepository(
    private val rewardsDao: RewardsDao,
    private val redemptionsDao: RedemptionsDao
) {
    
    // Reward management
    suspend fun getAllActiveRewards(): List<Reward> = rewardsDao.getAllActiveRewards()
    
    suspend fun getTemplateRewards(): List<Reward> = rewardsDao.getTemplateRewards()
    
    suspend fun getCustomRewards(): List<Reward> = rewardsDao.getCustomRewards()
    
    suspend fun getRewardById(rewardId: String): Reward? = rewardsDao.getRewardById(rewardId)
    
    suspend fun createCustomReward(title: String, notes: String?, costPoints: Int?): Result<Reward> {
        return try {
            val reward = Reward(
                id = UUID.randomUUID().toString(),
                title = title,
                notes = notes,
                costPoints = costPoints,
                template = false
            )
            rewardsDao.insertReward(reward)
            Result.success(reward)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createCustomRewardWithMilestones(
        title: String, 
        notes: String?, 
        costPoints: Int?, 
        milestoneCount: Int, 
        milestoneType: MilestoneType?
    ): Result<Reward> {
        return try {
            val reward = Reward(
                id = UUID.randomUUID().toString(),
                title = title,
                notes = notes,
                costPoints = costPoints,
                template = false,
                requiredMilestoneCount = milestoneCount,
                milestoneType = milestoneType
            )
            rewardsDao.insertReward(reward)
            Result.success(reward)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateReward(reward: Reward): Result<Unit> {
        return try {
            rewardsDao.updateReward(reward)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteReward(reward: Reward): Result<Unit> {
        return try {
            rewardsDao.deactivateReward(reward.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Redemption management
    suspend fun claimReward(rewardId: String, eventRef: String, claimSource: String): Result<Redemption> {
        return try {
            val redemption = Redemption(
                id = UUID.randomUUID().toString(),
                rewardId = rewardId,
                eventRef = eventRef,
                claimSource = claimSource
            )
            redemptionsDao.insertRedemption(redemption)
            Result.success(redemption)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllRedemptions(): List<Redemption> = redemptionsDao.getAllRedemptions()
    
    suspend fun getRedemptionsForReward(rewardId: String): List<Redemption> = 
        redemptionsDao.getRedemptionsForReward(rewardId)
    
    suspend fun getRedemptionCount(rewardId: String): Int = 
        redemptionsDao.getRedemptionCount(rewardId)
    
    // Template rewards initialization
    suspend fun initializeTemplateRewards() {
        val existingTemplates = getTemplateRewards()
        if (existingTemplates.isEmpty()) {
            val templates = getDefaultTemplateRewards()
            templates.forEach { template ->
                rewardsDao.insertReward(template)
            }
        }
    }
    
    private fun getDefaultTemplateRewards(): List<Reward> {
        return listOf(
            Reward(
                id = "template_movie_night",
                title = "Movie night",
                notes = "Pick a film you've been wanting to watch",
                template = true,
                requiredMilestoneCount = 1
            ),
            Reward(
                id = "template_fancy_coffee",
                title = "Fancy coffee",
                notes = "Treat yourself to that special blend",
                template = true,
                requiredMilestoneCount = 1
            ),
            Reward(
                id = "template_takeout",
                title = "Favorite takeout",
                notes = "Order from your favorite restaurant",
                template = true,
                requiredMilestoneCount = 2
            ),
            Reward(
                id = "template_book",
                title = "New book",
                notes = "Pick up something you've been meaning to read",
                template = true,
                requiredMilestoneCount = 1
            ),
            Reward(
                id = "template_bath",
                title = "Long relaxing bath",
                notes = "With candles, music, whatever makes it special",
                template = true,
                requiredMilestoneCount = 1
            ),
            Reward(
                id = "template_walk",
                title = "Nature walk",
                notes = "Visit that place you've been meaning to explore",
                template = true,
                requiredMilestoneCount = 1
            ),
            Reward(
                id = "template_music",
                title = "New music",
                notes = "Buy an album or concert tickets",
                template = true,
                requiredMilestoneCount = 3,
                milestoneType = MilestoneType.PROGRESS
            ),
            Reward(
                id = "template_hobby",
                title = "Hobby supplies",
                notes = "Get something for your favorite hobby",
                template = true,
                requiredMilestoneCount = 2,
                milestoneType = MilestoneType.AVOIDANCE
            ),
            Reward(
                id = "template_new_clothes",
                title = "New clothes",
                notes = "Treat yourself to something you've been wanting",
                template = true,
                requiredMilestoneCount = 2
            ),
            Reward(
                id = "template_kids_bedroom_item",
                title = "Kids bedroom item",
                notes = "Something special for the kids' room",
                template = true,
                requiredMilestoneCount = 1,
                milestoneType = MilestoneType.AVOIDANCE
            ),
            Reward(
                id = "template_kids_treat",
                title = "Kids treat",
                notes = "A special treat or experience for the kids",
                template = true,
                requiredMilestoneCount = 1
            )
        )
    }
}