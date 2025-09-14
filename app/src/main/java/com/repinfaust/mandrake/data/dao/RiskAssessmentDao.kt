package com.repinfaust.mandrake.data.dao

import androidx.room.*
import com.repinfaust.mandrake.data.entity.RiskAssessment
import com.repinfaust.mandrake.data.entity.NudgeTier

@Dao
interface RiskAssessmentDao {
    @Query("SELECT * FROM risk_assessments ORDER BY timestamp DESC")
    suspend fun getAllAssessments(): List<RiskAssessment>
    
    @Query("SELECT * FROM risk_assessments ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAssessment(): RiskAssessment?
    
    @Query("SELECT * FROM risk_assessments WHERE nudgeTier != 'NONE' ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentNudges(): List<RiskAssessment>
    
    @Query("SELECT * FROM risk_assessments WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getAssessmentsSince(startTime: Long): List<RiskAssessment>
    
    @Query("SELECT * FROM risk_assessments WHERE nudgeTier = :tier AND timestamp >= :startTime")
    suspend fun getAssessmentsWithTierSince(tier: NudgeTier, startTime: Long): List<RiskAssessment>
    
    @Insert
    suspend fun insertAssessment(assessment: RiskAssessment): Long
    
    @Update
    suspend fun updateAssessment(assessment: RiskAssessment)
    
    @Delete
    suspend fun deleteAssessment(assessment: RiskAssessment)
    
    @Query("DELETE FROM risk_assessments WHERE timestamp < :beforeTime")
    suspend fun deleteOldAssessments(beforeTime: Long)
}