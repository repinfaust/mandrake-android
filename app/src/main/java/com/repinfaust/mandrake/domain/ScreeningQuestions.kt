package com.repinfaust.mandrake.domain

import com.repinfaust.mandrake.data.entity.RiskBand
import com.repinfaust.mandrake.data.entity.ScreeningType

/**
 * Screening questionnaires for risk assessment
 * AUDIT-C for alcohol, SDS adapted for other substances/behaviors
 * All scoring follows clinical guidelines but presents user-friendly bands
 */

data class ScreeningQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val scores: List<Int>
)

object ScreeningQuestions {
    
    // AUDIT-C Questions (Alcohol Use Disorders Identification Test - Consumption)
    fun getAuditCQuestions(): List<ScreeningQuestion> = listOf(
        ScreeningQuestion(
            id = "audit_c_1",
            text = "How often do you have a drink containing alcohol?",
            options = listOf("Never", "Monthly or less", "2-4 times a month", "2-3 times a week", "4+ times a week"),
            scores = listOf(0, 1, 2, 3, 4)
        ),
        ScreeningQuestion(
            id = "audit_c_2", 
            text = "How many standard drinks containing alcohol do you have on a typical day when drinking?",
            options = listOf("1-2", "3-4", "5-6", "7-9", "10+"),
            scores = listOf(0, 1, 2, 3, 4)
        ),
        ScreeningQuestion(
            id = "audit_c_3",
            text = "How often do you have six or more drinks on one occasion?",
            options = listOf("Never", "Less than monthly", "Monthly", "Weekly", "Daily or almost daily"),
            scores = listOf(0, 1, 2, 3, 4)
        )
    )
    
    // SDS Questions adapted for substances/behaviors
    fun getSdsQuestions(category: String): List<ScreeningQuestion> {
        val activityName = when (category.lowercase()) {
            "cannabis" -> "cannabis"
            "gambling" -> "gambling"
            "gaming" -> "gaming"
            "shopping" -> "shopping"
            "social_media" -> "social media"
            else -> "this activity"
        }
        
        return listOf(
            ScreeningQuestion(
                id = "sds_1",
                text = "Did you think your use of $activityName was out of control?",
                options = listOf("Never/almost never", "Sometimes", "Often", "Always/nearly always"),
                scores = listOf(0, 1, 2, 3)
            ),
            ScreeningQuestion(
                id = "sds_2",
                text = "Did the prospect of missing $activityName make you anxious or worried?",
                options = listOf("Never/almost never", "Sometimes", "Often", "Always/nearly always"),
                scores = listOf(0, 1, 2, 3)
            ),
            ScreeningQuestion(
                id = "sds_3",
                text = "Did you worry about your use of $activityName?",
                options = listOf("Never/almost never", "Sometimes", "Often", "Always/nearly always"),
                scores = listOf(0, 1, 2, 3)
            ),
            ScreeningQuestion(
                id = "sds_4", 
                text = "Did you wish you could stop using $activityName?",
                options = listOf("Never/almost never", "Sometimes", "Often", "Always/nearly always"),
                scores = listOf(0, 1, 2, 3)
            ),
            ScreeningQuestion(
                id = "sds_5",
                text = "How difficult would it be for you to stop or go without $activityName?",
                options = listOf("Not difficult", "Quite difficult", "Very difficult", "Impossible"),
                scores = listOf(0, 1, 2, 3)
            )
        )
    }
    
    // Red flag mini-checks (yes/no)
    fun getRedFlagQuestions(category: String): List<ScreeningQuestion> {
        val activityName = when (category.lowercase()) {
            "alcohol" -> "drinking"
            "cannabis" -> "using cannabis"
            "gambling" -> "gambling"
            "gaming" -> "gaming"
            "shopping" -> "shopping"
            "social_media" -> "using social media"
            else -> "this activity"
        }
        
        return listOf(
            ScreeningQuestion(
                id = "red_flag_morning",
                text = "Do you typically engage in $activityName first thing in the morning?",
                options = listOf("No", "Yes"),
                scores = listOf(0, 1)
            ),
            ScreeningQuestion(
                id = "red_flag_withdrawal",
                text = "Do you experience shakes, sweats, anxiety, or other uncomfortable feelings when you try to stop $activityName?",
                options = listOf("No", "Yes"),
                scores = listOf(0, 1)
            ),
            ScreeningQuestion(
                id = "red_flag_blackout",
                text = "Have you experienced memory gaps or blackouts related to $activityName?",
                options = listOf("No", "Yes"),
                scores = listOf(0, 1)
            ),
            ScreeningQuestion(
                id = "red_flag_failed_cutdown",
                text = "Have you tried to cut down on $activityName for 2-4 weeks but found it difficult to maintain the change?",
                options = listOf("No", "Yes"),
                scores = listOf(0, 1)
            )
        )
    }
    
    // Scoring functions
    fun scoreAuditC(responses: List<Int>): RiskBand {
        val total = responses.sum()
        return when {
            total <= 2 -> RiskBand.LOW
            total <= 4 -> RiskBand.ELEVATED  
            else -> RiskBand.HIGH
        }
    }
    
    fun scoreSds(responses: List<Int>): RiskBand {
        val total = responses.sum()
        return when {
            total <= 3 -> RiskBand.LOW
            total <= 6 -> RiskBand.ELEVATED
            else -> RiskBand.HIGH
        }
    }
    
    fun getBandDescription(band: RiskBand): String = when (band) {
        RiskBand.LOW -> "Your current pattern looks Low risk."
        RiskBand.ELEVATED -> "Your current pattern looks Elevated risk."
        RiskBand.HIGH -> "Your current pattern looks High risk."
    }
    
    fun getBandSuggestion(band: RiskBand): String = when (band) {
        RiskBand.LOW -> "Keep doing what works. Wave timer is optional."
        RiskBand.ELEVATED -> "Pause for a breath, then choose an alternative."
        RiskBand.HIGH -> "Consider adding one confidential support. Options are in Support."
    }
}