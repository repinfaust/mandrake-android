package com.repinfaust.mandrake.data.repo

import com.repinfaust.mandrake.data.dao.ScreeningDao
import com.repinfaust.mandrake.data.dao.RiskAssessmentDao
import com.repinfaust.mandrake.data.entity.*
import com.repinfaust.mandrake.domain.RiskEvaluationEngine
import com.repinfaust.mandrake.domain.ScreeningQuestions
import java.time.LocalDateTime
import java.time.ZoneOffset

class RiskAssessmentRepository(
    private val screeningDao: ScreeningDao,
    private val riskAssessmentDao: RiskAssessmentDao,
    private val riskEvaluationEngine: RiskEvaluationEngine = RiskEvaluationEngine()
) {
    
    // Screening operations
    suspend fun saveScreeningResult(
        type: ScreeningType,
        category: String,
        responses: List<Int>
    ): Result<ScreeningResult> {
        return try {
            val band = when (type) {
                ScreeningType.AUDIT_C -> ScreeningQuestions.scoreAuditC(responses)
                ScreeningType.SDS -> ScreeningQuestions.scoreSds(responses)
            }
            
            val screening = ScreeningResult(
                type = type,
                category = category,
                responses = responses,
                band = band,
                isSkipped = false
            )
            
            screeningDao.insertScreening(screening)
            Result.success(screening)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun skipScreening(type: ScreeningType, category: String): Result<ScreeningResult> {
        return try {
            val screening = ScreeningResult(
                type = type,
                category = category,
                responses = emptyList(),
                band = RiskBand.LOW,
                isSkipped = true
            )
            
            screeningDao.insertScreening(screening)
            Result.success(screening)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLatestScreeningForCategory(category: String): ScreeningResult? {
        return screeningDao.getLatestScreeningForCategory(category)
    }
    
    suspend fun getAllScreenings(): List<ScreeningResult> {
        return screeningDao.getAllScreenings()
    }
    
    suspend fun shouldTriggerScreening(
        category: String,
        recentHighIntensityCount: Int
    ): Boolean {
        val lastScreening = getLatestScreeningForCategory(category)
        val daysSinceLastScreening = if (lastScreening != null) {
            val lastScreeningTime = LocalDateTime.ofEpochSecond(lastScreening.timestamp / 1000, 0, ZoneOffset.UTC)
            val now = LocalDateTime.now()
            java.time.Duration.between(lastScreeningTime, now).toDays().toInt()
        } else {
            999 // No previous screening
        }
        
        return riskEvaluationEngine.shouldTriggerScreening(
            lastScreening,
            daysSinceLastScreening,
            recentHighIntensityCount
        )
    }
    
    // Risk assessment operations - BEHAVIORAL PATTERN ANALYSIS
    suspend fun performBehavioralRiskAssessment(
        events: List<UrgeEvent>,
        contextualRedFlags: RedFlags = RedFlags()
    ): Result<RiskAssessment> {
        return try {
            // Get latest screening band from SEPARATE triage system
            val latestScreeningBand = getLatestOverallScreeningBand()
            
            // Use risk engine for BEHAVIORAL analysis only
            val assessment = riskEvaluationEngine.evaluateCurrentRisk(
                events = events, 
                latestScreeningBand = latestScreeningBand,
                contextualRedFlags = contextualRedFlags
            )
            
            riskAssessmentDao.insertAssessment(assessment)
            Result.success(assessment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get overall risk band from latest triage screenings
     * This is separate from behavioral risk assessment
     */
    private suspend fun getLatestOverallScreeningBand(): RiskBand {
        val allScreenings = getAllScreenings()
        if (allScreenings.isEmpty()) return RiskBand.LOW
        
        // Get latest screening for each category
        val latestByCategory = allScreenings
            .filter { !it.isSkipped }
            .groupBy { it.category }
            .mapValues { (_, categoryScreenings) ->
                categoryScreenings.maxByOrNull { it.timestamp }?.band ?: RiskBand.LOW
            }
        
        // Return highest risk band across all categories
        val allBands = latestByCategory.values
        return when {
            allBands.contains(RiskBand.HIGH) -> RiskBand.HIGH
            allBands.contains(RiskBand.ELEVATED) -> RiskBand.ELEVATED
            else -> RiskBand.LOW
        }
    }
    
    suspend fun getLatestRiskAssessment(): RiskAssessment? {
        return riskAssessmentDao.getLatestAssessment()
    }
    
    suspend fun getRecentNudges(): List<RiskAssessment> {
        return riskAssessmentDao.getRecentNudges()
    }
    
    suspend fun shouldShowNudge(): Boolean {
        val latestAssessment = getLatestRiskAssessment()
        val recentNudges = getRecentNudges()
        
        val hoursSinceLastNudge = if (recentNudges.isNotEmpty()) {
            val lastNudgeTime = recentNudges.first().timestamp
            val now = System.currentTimeMillis()
            (now - lastNudgeTime) / (1000 * 60 * 60) // Convert to hours
        } else {
            999L // No recent nudges
        }
        
        return riskEvaluationEngine.shouldShowNudge(
            latestAssessment,
            recentNudges,
            hoursSinceLastNudge
        )
    }
    
    // Clean up old data
    suspend fun cleanupOldData() {
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30).toInstant(ZoneOffset.UTC).toEpochMilli()
        riskAssessmentDao.deleteOldAssessments(thirtyDaysAgo)
    }
    
    // Data export/privacy
    suspend fun exportScreeningData(): List<ScreeningResult> {
        return getAllScreenings()
    }
    
    suspend fun exportRiskAssessmentData(): List<RiskAssessment> {
        return riskAssessmentDao.getAllAssessments()
    }
    
    suspend fun deleteAllScreeningData() {
        val screenings = getAllScreenings()
        screenings.forEach { screening ->
            screeningDao.deleteScreening(screening)
        }
    }
    
    suspend fun deleteAllRiskAssessmentData() {
        val assessments = riskAssessmentDao.getAllAssessments()
        assessments.forEach { assessment ->
            riskAssessmentDao.deleteAssessment(assessment)
        }
    }
}