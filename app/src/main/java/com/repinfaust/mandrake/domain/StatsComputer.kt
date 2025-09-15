package com.repinfaust.mandrake.domain
import com.repinfaust.mandrake.data.entity.Milestone
import com.repinfaust.mandrake.data.entity.MilestoneType
import com.repinfaust.mandrake.data.entity.UrgeEvent
import com.repinfaust.mandrake.data.entity.EventType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object StatsComputer {
  data class HeatCell(val dayOfWeek: Int, val hour: Int, val count: Int)
  fun weeklyHeatmap(events: List<UrgeEvent>, zone: ZoneId): List<HeatCell> {
    val grid = mutableMapOf<Pair<Int,Int>, Int>()
    events.forEach { e ->
      val z = ZonedDateTime.ofInstant(Instant.ofEpochMilli(e.timestamp), zone)
      val key = (z.dayOfWeek.value % 7) to z.hour
      grid[key] = (grid[key] ?: 0) + 1
    }
    return grid.map { (k,v) -> HeatCell(k.first, k.second, v) }
  }
  fun topTactics(events: List<UrgeEvent>): List<Pair<String, Int>> =
    events.filter { !it.gaveIn && it.tactic != null }
      .groupBy { it.tactic!!.name }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
  
  fun calculateMilestones(events: List<UrgeEvent>): List<Milestone> {
    if (events.isEmpty()) return Milestone.ALL_MILESTONES
    
    // Count events by type for new milestone system
    val bypassedUrges = events.count { it.eventType == EventType.BYPASSED_URGE }
    val avoidedTasks = events.count { it.eventType == EventType.AVOIDED_TASK }
    
    android.util.Log.d("MilestoneCalculation", "Bypassed: $bypassedUrges, Avoided: $avoidedTasks")
    
    return Milestone.ALL_MILESTONES.map { milestone ->
      val isAchieved = when (milestone.type) {
        MilestoneType.PROGRESS -> bypassedUrges >= milestone.hoursRequired
        MilestoneType.AVOIDANCE -> avoidedTasks >= milestone.hoursRequired
      }
      
      android.util.Log.d("MilestoneCalculation", "${milestone.name}: required=${milestone.hoursRequired}, achieved=$isAchieved")
      
      milestone.copy(isAchieved = isAchieved)
    }
  }
  
  fun getLatestAchievedMilestone(events: List<UrgeEvent>): Milestone? {
    val achievedMilestones = calculateMilestones(events)
      .filter { it.isAchieved }
    
    android.util.Log.d("MilestoneCelebration", "Achieved milestones: ${achievedMilestones.map { "${it.name}:${it.hoursRequired}" }}")
    
    return achievedMilestones.maxByOrNull { it.hoursRequired }
  }
}
