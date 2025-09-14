package com.repinfaust.mandrake.domain.logic

import com.repinfaust.mandrake.domain.model.*
import com.repinfaust.mandrake.data.entity.TriggerType
import java.time.LocalDateTime

class NextRightActionManager {
    
    private fun createDefaultActionItems(): List<NextRightActionItem> {
        val items = mutableListOf<NextRightActionItem>()
        
        ActionDomain.values().forEach { domain ->
            ActionTemplates.getTemplates(domain).forEach { action ->
                items.add(
                    NextRightActionItem(
                        id = "${domain.name.lowercase()}_${action.hashCode()}",
                        category = domain,
                        text = action,
                        defaultMinutes = getDefaultMinutes(action),
                        userMinutes = getDefaultMinutes(action)
                    )
                )
            }
        }
        
        return items
    }
    
    private fun getDefaultMinutes(action: String): Int {
        return when {
            action.contains("call") -> 3
            action.contains("15-min") -> 15
            action.contains("10-min") -> 10
            action.contains("email") -> 2
            action.contains("photograph") -> 1
            action.contains("breath") -> 1
            action.contains("water") -> 1
            else -> 2
        }
    }
    
    fun pickNextRightAction(
        actionItems: List<NextRightActionItem> = createDefaultActionItems(),
        recentTriggers: List<TriggerType>,
        now: LocalDateTime = LocalDateTime.now()
    ): NextRightActionItem? {
        // Get top trigger category
        val topTrigger = getTopTriggerDomain(recentTriggers)
        
        // Filter available candidates
        val candidates = actionItems
            .filter { it.canShowOnHome() && it.isAvailable(now) }
            .sortedBy { it.userMinutes }
        
        if (candidates.isEmpty()) return null
        
        // Prefer actions matching top trigger, otherwise take first available
        return candidates.find { it.category == topTrigger } ?: candidates.first()
    }
    
    fun getNextCandidate(
        currentItem: NextRightActionItem,
        actionItems: List<NextRightActionItem> = createDefaultActionItems(),
        now: LocalDateTime = LocalDateTime.now()
    ): NextRightActionItem? {
        val candidates = actionItems
            .filter { it.canShowOnHome() && it.isAvailable(now) && it.id != currentItem.id }
            .sortedBy { it.userMinutes }
            
        return candidates.firstOrNull()
    }
    
    private fun getTopTriggerDomain(recentTriggers: List<TriggerType>): ActionDomain {
        val triggerCounts = recentTriggers.groupingBy { it }.eachCount()
        
        return when {
            triggerCounts.getOrDefault(TriggerType.MONEY, 0) > 2 -> ActionDomain.MONEY
            triggerCounts.getOrDefault(TriggerType.LONELY, 0) > 1 ||
            triggerCounts.getOrDefault(TriggerType.SOCIAL, 0) > 1 -> ActionDomain.SOCIAL
            triggerCounts.getOrDefault(TriggerType.STRESS, 0) > 1 ||
            triggerCounts.getOrDefault(TriggerType.ANXIETY, 0) > 1 -> ActionDomain.REPAIR
            triggerCounts.getOrDefault(TriggerType.FATIGUE, 0) > 1 -> ActionDomain.BODY
            triggerCounts.getOrDefault(TriggerType.BOREDOM, 0) > 1 -> ActionDomain.ENVIRONMENT
            else -> ActionDomain.ADMIN
        }
    }
    
    fun selectDomainForUser(recentTriggers: List<TriggerType>): ActionDomain {
        return getTopTriggerDomain(recentTriggers)
    }
    
    fun selectTemplateForDomain(
        domain: ActionDomain,
        recentlyUsedTemplates: List<String>
    ): String {
        val allTemplates = ActionTemplates.getTemplates(domain)
        val availableTemplates = allTemplates.filter { template ->
            template !in recentlyUsedTemplates
        }
        
        return if (availableTemplates.isNotEmpty()) {
            availableTemplates.random()
        } else {
            // If all have been used recently, pick randomly from all
            allTemplates.random()
        }
    }
    
    fun getNextTemplate(
        currentDomain: ActionDomain,
        currentTemplate: String
    ): Pair<ActionDomain, String> {
        val domains = ActionDomain.values()
        val nextDomainIndex = (domains.indexOf(currentDomain) + 1) % domains.size
        val nextDomain = domains[nextDomainIndex]
        
        val templates = ActionTemplates.getTemplates(nextDomain)
        val nextTemplate = templates.first()
        
        return nextDomain to nextTemplate
    }
    
    fun shouldShowNextRightAction(
        urgeCardOpen: Boolean,
        urgeIntensity: Int,
        waveTimerRunning: Boolean,
        lastUrgeFlowTime: LocalDateTime?
    ): Boolean {
        if (urgeCardOpen || waveTimerRunning) {
            return false
        }
        
        lastUrgeFlowTime?.let { lastTime ->
            val fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15)
            if (lastTime.isAfter(fifteenMinutesAgo)) {
                return false
            }
        }
        
        return true
    }
    
    fun calculatePoints(completedActionsToday: Int): Int {
        return when {
            completedActionsToday < 2 -> 10 // Same as "Alternative chosen"
            else -> 5 // Half points for 3rd+ action of the day
        }
    }
}