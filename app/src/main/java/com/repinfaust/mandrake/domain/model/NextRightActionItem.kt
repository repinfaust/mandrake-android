package com.repinfaust.mandrake.domain.model

import java.time.LocalDateTime

data class NextRightActionItem(
    val id: String,
    val category: ActionDomain,
    val text: String,
    val defaultMinutes: Int = 2,
    val userMinutes: Int = 2,
    val maxHomeMinutes: Int = 5,
    val lastDoneAt: LocalDateTime? = null,
    val snoozedUntil: LocalDateTime? = null,
    val timesDone: Int = 0
) {
    fun canShowOnHome(): Boolean {
        return userMinutes <= maxHomeMinutes
    }
    
    fun isAvailable(now: LocalDateTime): Boolean {
        // Check if snoozed
        snoozedUntil?.let { snoozeTime ->
            if (now.isBefore(snoozeTime)) return false
        }
        
        // Check 72-hour cooldown
        lastDoneAt?.let { lastTime ->
            if (now.isBefore(lastTime.plusHours(72))) return false
        }
        
        return true
    }
    
    fun withCompleted(now: LocalDateTime): NextRightActionItem {
        return copy(
            lastDoneAt = now,
            timesDone = timesDone + 1
        )
    }
    
    fun withSnoozed(until: LocalDateTime): NextRightActionItem {
        return copy(snoozedUntil = until)
    }
    
    fun withCustomTime(minutes: Int): NextRightActionItem {
        return copy(userMinutes = minutes)
    }
}