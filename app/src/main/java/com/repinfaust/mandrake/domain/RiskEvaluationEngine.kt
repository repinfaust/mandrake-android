package com.repinfaust.mandrake.domain

import com.repinfaust.mandrake.data.entity.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.max

/**
 * Risk evaluation engine for DYNAMIC, ONGOING behavioral pattern analysis
 * 
 * Responsibility: Continuously monitor user behavior between screenings
 * Input: Urge logs, event patterns, behavioral data
 * Output: Real-time escalation nudges (None/Soft/Firm)
 * Frequency: After each event + rolling window analysis
 * 
 * NOTE: This is separate from Triage system which handles static screening questionnaires
 */
class RiskEvaluationEngine {
    
    /**
     * Evaluates current behavioral patterns and determines if escalation nudge needed
     * Uses screening data as context but doesn't duplicate triage risk banding
     */
    fun evaluateCurrentRisk(
        events: List<UrgeEvent>,
        latestScreeningBand: RiskBand? = null,
        contextualRedFlags: RedFlags = RedFlags(),
        now: LocalDateTime = LocalDateTime.now()
    ): RiskAssessment {
        
        // Calculate time windows
        val nowMillis = now.toInstant(ZoneOffset.UTC).toEpochMilli()
        val sevenDaysAgo = now.minusDays(7).toInstant(ZoneOffset.UTC).toEpochMilli()
        val fourteenDaysAgo = now.minusDays(14).toInstant(ZoneOffset.UTC).toEpochMilli()
        
        // Filter events for time windows
        val events7d = events.filter { it.timestamp >= sevenDaysAgo }
        val events14d = events.filter { it.timestamp >= fourteenDaysAgo }
        
        // Calculate behavioral metrics
        val urges7d = events7d.size
        val highIntensity7d = events7d.count { it.intensity >= 7 }
        val nightEpisodes7d = events7d.count { isNighttime(it.timestamp) }
        
        // Calculate acted vs alternatives ratio
        val acted14d = events14d.count { it.gaveIn }
        val alternatives14d = events14d.count { !it.gaveIn }
        val actedVsAlt14d = if (alternatives14d > 0) acted14d.toDouble() / alternatives14d else 0.0
        
        // Use provided screening band (from separate triage system)
        val screenerBand = latestScreeningBand ?: RiskBand.LOW
        
        // Create risk inputs for behavioral analysis
        val riskInputs = RiskInputs(
            screenerBand = screenerBand,
            urges7d = urges7d,
            highIntensity7d = highIntensity7d,
            actedVsAlt14d = actedVsAlt14d,
            nightEpisodes7d = nightEpisodes7d,
            redFlags = contextualRedFlags
        )
        
        // Evaluate nudge tier
        val nudgeTier = RiskRules.evaluateNudge(riskInputs)
        val ruleTriggered = determineRuleTriggered(riskInputs, nudgeTier)
        
        return RiskAssessment(
            timestamp = nowMillis,
            screenerBand = screenerBand,
            urges7d = urges7d,
            highIntensity7d = highIntensity7d,
            actedVsAlt14d = actedVsAlt14d,
            nightEpisodes7d = nightEpisodes7d,
            redFlags = contextualRedFlags,
            nudgeTier = nudgeTier,
            ruleTriggered = ruleTriggered
        )
    }
    
    private fun isNighttime(timestampMillis: Long): Boolean {
        val dateTime = LocalDateTime.ofEpochSecond(timestampMillis / 1000, 0, ZoneOffset.UTC)
        val hour = dateTime.hour
        return hour in 0..5 // 00:00 to 05:00
    }
    
    /**
     * This function is now handled by the separate Triage system
     * Risk Engine only receives the latest screening band as input
     */
    
    private fun determineRuleTriggered(inputs: RiskInputs, tier: NudgeTier): String? {
        return when (tier) {
            NudgeTier.FIRM -> {
                when {
                    inputs.redFlags.withdrawal -> "withdrawal_symptoms"
                    inputs.redFlags.blackout -> "blackout_episodes"
                    inputs.screenerBand == RiskBand.HIGH -> "high_screener_band"
                    inputs.actedVsAlt14d > 1.0 && inputs.urges7d >= 5 -> "high_frequency_pattern"
                    inputs.actedVsAlt14d > 1.0 && inputs.highIntensity7d >= 3 -> "high_intensity_pattern"
                    else -> "multiple_risk_factors"
                }
            }
            NudgeTier.SOFT -> {
                when {
                    inputs.nightEpisodes7d >= 3 -> "night_episodes"
                    inputs.screenerBand == RiskBand.ELEVATED -> "elevated_screener_with_behavior"
                    else -> "emerging_pattern"
                }
            }
            NudgeTier.NONE -> null
        }
    }
    
    fun shouldTriggerScreening(
        lastScreening: ScreeningResult?,
        daysSinceLastScreening: Int,
        recentHighIntensityCount: Int
    ): Boolean {
        // Onboarding: No previous screening
        if (lastScreening == null) return true
        
        // Periodic check-in: every 14 days
        if (daysSinceLastScreening >= 14) return true
        
        // Contextual mini-check: after several high-intensity entries
        if (recentHighIntensityCount >= 3 && daysSinceLastScreening >= 3) return true
        
        return false
    }
    
    fun shouldShowNudge(
        latestAssessment: RiskAssessment?,
        recentNudges: List<RiskAssessment>,
        hoursSinceLastNudge: Long
    ): Boolean {
        // No assessment or assessment shows no risk
        if (latestAssessment?.nudgeTier == null || latestAssessment.nudgeTier == NudgeTier.NONE) {
            return false
        }
        
        // Don't show too frequently
        val cooldownHours = when (latestAssessment.nudgeTier) {
            NudgeTier.SOFT -> 24  // Once per day max
            NudgeTier.FIRM -> 12  // More urgent, can show twice per day
            NudgeTier.NONE -> return false
        }
        
        if (hoursSinceLastNudge < cooldownHours) return false
        
        // Check if we've shown this tier recently
        val now = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        val recentSameTier = recentNudges.count { 
            it.nudgeTier == latestAssessment.nudgeTier && 
            (now - it.timestamp) < (cooldownHours * 3600 * 1000) // Convert hours to millis
        }
        
        // Limit frequency of same-tier nudges
        return when (latestAssessment.nudgeTier) {
            NudgeTier.SOFT -> recentSameTier < 2  // Max 2 soft nudges per day
            NudgeTier.FIRM -> recentSameTier < 3  // Max 3 firm nudges per day (more urgent)
            NudgeTier.NONE -> false
        }
    }
}