package com.repinfaust.mandrake.domain

import com.repinfaust.mandrake.data.entity.NudgeTier
import com.repinfaust.mandrake.data.entity.RedFlags
import com.repinfaust.mandrake.data.entity.RiskBand

/**
 * Mandrake — Soft Triage & Escalation rule engine (local-only, non-clinical)
 * Kotlin port of riskRules.ts with zero dependencies
 * 
 * Notes:
 * - Keep all data local-first. Do not sync without explicit user consent.
 * - This file encodes *behavioral* rules; it makes no diagnoses or treatment claims.
 */

data class RiskInputs(
    // Highest band across relevant screeners (e.g., AUDIT-C, SDS) for the user-selected category
    val screenerBand: RiskBand,
    
    // Recent behavior windows (app should compute these counts; this file stays pure)
    val urges7d: Int,            // total urges logged in the last 7 days
    val highIntensity7d: Int,    // urges with intensity >= 7 in the last 7 days
    val actedVsAlt14d: Double,   // ratio: (went with urge) / (chose alternative) over last 14 days
    val nightEpisodes7d: Int,    // episodes between 00:00–05:00 in the last 7 days
    
    // Red flags from contextual mini-checks (boolean chips)
    val redFlags: RedFlags
)

data class Thresholds(
    // Firm triggers
    val highScreenerIsFirm: Boolean = true,         // if true, Band 'high' alone produces a firm nudge
    val actedVsAltFirmRatio: Double = 1.0,         // > this ratio combined with volume triggers -> firm
    val urges7dFirmMin: Int = 5,                   // volume partner for firm
    val highIntensity7dFirmMin: Int = 3,           // intensity partner for firm
    
    // Soft triggers
    val nightEpisodesSoftMin: Int = 3,             // >= this → soft
    val elevatedScreenerSoft: Boolean = true,      // if true, 'elevated' can produce soft with counts below
    val urges7dSoftMin: Int = 4,
    val highIntensity7dSoftMin: Int = 2
)

object RiskRules {
    
    val DEFAULT_THRESHOLDS = Thresholds()
    
    fun evaluateNudge(inputs: RiskInputs, thresholds: Thresholds = DEFAULT_THRESHOLDS): NudgeTier {
        // Hard safety first
        if (inputs.redFlags.withdrawal || inputs.redFlags.blackout) return NudgeTier.FIRM
        
        // High screener or multiple strong signals
        if (thresholds.highScreenerIsFirm && inputs.screenerBand == RiskBand.HIGH) return NudgeTier.FIRM
        if (inputs.actedVsAlt14d > thresholds.actedVsAltFirmRatio && 
            (inputs.urges7d >= thresholds.urges7dFirmMin || inputs.highIntensity7d >= thresholds.highIntensity7dFirmMin)) {
            return NudgeTier.FIRM
        }
        
        // Emerging pattern → soft
        if (inputs.nightEpisodes7d >= thresholds.nightEpisodesSoftMin) return NudgeTier.SOFT
        if (thresholds.elevatedScreenerSoft && 
            inputs.screenerBand == RiskBand.ELEVATED && 
            (inputs.highIntensity7d >= thresholds.highIntensity7dSoftMin || inputs.urges7d >= thresholds.urges7dSoftMin)) {
            return NudgeTier.SOFT
        }
        
        return NudgeTier.NONE
    }
    
    /**
     * Get nudge content with improved tone and messaging
     * Based on escalation tier mapping from refinement notes
     */
    fun getNudgeContent(tier: NudgeTier, context: String = ""): NudgeContent {
        return when (tier) {
            NudgeTier.SOFT -> getSoftNudgeContent(context)
            NudgeTier.FIRM -> getFirmNudgeContent(context) 
            NudgeTier.NONE -> NudgeContent(tier, "", "")
        }
    }
    
    private fun getSoftNudgeContent(context: String): NudgeContent {
        // Gentle, supportive, non-intrusive tone
        val messages = listOf(
            NudgeContent(
                tier = NudgeTier.SOFT,
                title = "You've logged a few heavy urges this week",
                body = "Want to try a quick check-in? It might help spot what's working best for you."
            ),
            NudgeContent(
                tier = NudgeTier.SOFT,
                title = "Looks like late-night urges are popping up",
                body = "Here are some tools that work well in those moments: wave timer, quick alternatives, or just logging how you're feeling."
            ),
            NudgeContent(
                tier = NudgeTier.SOFT,
                title = "Patterns are shifting a bit",
                body = "No pressure — just wondered if you'd like to explore some gentle tools that might help. Everything stays private."
            )
        )
        return messages.random()
    }
    
    private fun getFirmNudgeContent(context: String): NudgeContent {
        // Clearer, more direct, but still user-controlled
        val messages = listOf(
            NudgeContent(
                tier = NudgeTier.FIRM,
                title = "This pattern looks heavy",
                body = "Would you like to see private support options? You're in control — we never block your flow, just offering what's available."
            ),
            NudgeContent(
                tier = NudgeTier.FIRM,
                title = "You've carried a lot solo",
                body = "One confidential option could help. Check Support for private resources like NHS 111, Samaritans, or local services."
            ),
            NudgeContent(
                tier = NudgeTier.FIRM,
                title = "Heavy moments deserve support",
                body = "There are confidential resources available if you want them. No judgment, no pressure — just options when you need them."
            )
        )
        return messages.random()
    }
    
    fun combineBands(alcoholBand: RiskBand?, otherBand: RiskBand?): RiskBand {
        // Highest-risk band wins
        val bands = listOfNotNull(alcoholBand, otherBand)
        return when {
            bands.contains(RiskBand.HIGH) -> RiskBand.HIGH
            bands.contains(RiskBand.ELEVATED) -> RiskBand.ELEVATED
            else -> RiskBand.LOW
        }
    }
}

data class NudgeContent(
    val tier: NudgeTier,
    val title: String,
    val body: String
)