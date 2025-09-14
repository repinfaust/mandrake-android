package com.repinfaust.mandrake.data.dao

import androidx.room.*
import com.repinfaust.mandrake.data.entity.Reward
import com.repinfaust.mandrake.data.entity.Redemption

@Dao
interface RewardsDao {
    @Query("SELECT * FROM rewards WHERE active = 1 ORDER BY createdAt DESC")
    suspend fun getAllActiveRewards(): List<Reward>
    
    @Query("SELECT * FROM rewards WHERE template = 1 AND active = 1")
    suspend fun getTemplateRewards(): List<Reward>
    
    @Query("SELECT * FROM rewards WHERE template = 0 AND active = 1 ORDER BY createdAt DESC")
    suspend fun getCustomRewards(): List<Reward>
    
    @Query("SELECT * FROM rewards WHERE id = :rewardId")
    suspend fun getRewardById(rewardId: String): Reward?
    
    @Insert
    suspend fun insertReward(reward: Reward)
    
    @Update
    suspend fun updateReward(reward: Reward)
    
    @Delete
    suspend fun deleteReward(reward: Reward)
    
    @Query("UPDATE rewards SET active = 0 WHERE id = :rewardId")
    suspend fun deactivateReward(rewardId: String)
}

@Dao
interface RedemptionsDao {
    @Query("SELECT * FROM redemptions ORDER BY timestamp DESC")
    suspend fun getAllRedemptions(): List<Redemption>
    
    @Query("SELECT * FROM redemptions WHERE rewardId = :rewardId ORDER BY timestamp DESC")
    suspend fun getRedemptionsForReward(rewardId: String): List<Redemption>
    
    @Query("SELECT * FROM redemptions WHERE timestamp >= :startTime")
    suspend fun getRedemptionsSince(startTime: Long): List<Redemption>
    
    @Query("SELECT COUNT(*) FROM redemptions WHERE rewardId = :rewardId")
    suspend fun getRedemptionCount(rewardId: String): Int
    
    @Insert
    suspend fun insertRedemption(redemption: Redemption)
    
    @Delete
    suspend fun deleteRedemption(redemption: Redemption)
    
    @Query("DELETE FROM redemptions WHERE rewardId = :rewardId")
    suspend fun deleteRedemptionsForReward(rewardId: String)
}