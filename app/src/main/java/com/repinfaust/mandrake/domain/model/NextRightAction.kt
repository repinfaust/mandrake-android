package com.repinfaust.mandrake.domain.model

import java.time.LocalDateTime

data class NextRightAction(
    val id: String,
    val domain: ActionDomain,
    val template: String,
    val completedAt: LocalDateTime? = null,
    val isRecurring: Boolean = false
)

enum class ActionDomain {
    MONEY, ADMIN, SOCIAL, REPAIR, ENVIRONMENT, BODY
}

object ActionTemplates {
    val MONEY = listOf(
        "Open one envelope",
        "Photograph a bill",
        "£5 token payment",
        "Call a creditor",
        "Book a 15-min review",
        "Cancel one subscription"
    )
    
    val ADMIN = listOf(
        "Reply to one email",
        "File one letter",
        "Rename & file one scan",
        "Update address",
        "Set a 10-min 'Open mail' block"
    )
    
    val SOCIAL = listOf(
        "Send a check-in message",
        "Ask for a 10-min call",
        "Send a 30-sec voice note",
        "RSVP to one invite",
        "Send one gratitude"
    )
    
    val REPAIR = listOf(
        "Draft an apology",
        "Write an I-statement",
        "Schedule a repair chat",
        "Delete an angry draft",
        "Write 3 calm lines"
    )
    
    val ENVIRONMENT = listOf(
        "Bin one item",
        "Clear 5 notifications",
        "Mute one trigger account",
        "Tray one clutter pile",
        "Set 1 app limit",
        "Hoover",
        "Washing",
        "Washing up",
        "Tidy up"
    )
    
    val BODY = listOf(
        "Drink water",
        "10 slow breaths",
        "2-minute stretch",
        "Door-to-door walk",
        "Eat one piece of fruit"
    )
    
    fun getTemplates(domain: ActionDomain): List<String> {
        return when (domain) {
            ActionDomain.MONEY -> MONEY
            ActionDomain.ADMIN -> ADMIN  
            ActionDomain.SOCIAL -> SOCIAL
            ActionDomain.REPAIR -> REPAIR
            ActionDomain.ENVIRONMENT -> ENVIRONMENT
            ActionDomain.BODY -> BODY
        }
    }
    
    fun getDomainLabel(domain: ActionDomain): String {
        return when (domain) {
            ActionDomain.MONEY -> "Money"
            ActionDomain.ADMIN -> "Admin"
            ActionDomain.SOCIAL -> "Social"
            ActionDomain.REPAIR -> "Repair"
            ActionDomain.ENVIRONMENT -> "Environment"
            ActionDomain.BODY -> "Body"
        }
    }
}