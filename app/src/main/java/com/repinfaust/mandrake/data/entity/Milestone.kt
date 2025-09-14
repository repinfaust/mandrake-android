package com.repinfaust.mandrake.data.entity

enum class MilestoneType {
  PROGRESS, AVOIDANCE
}

data class Milestone(
  val name: String,
  val description: String,
  val hoursRequired: Long,
  val emoji: String,
  val iconResource: String? = null,
  val type: MilestoneType = MilestoneType.PROGRESS,
  val isAchieved: Boolean = false
) {
  companion object {
  val ALL_MILESTONES = listOf(
    // Progress Milestones
    Milestone("First Momentum", "Take your first steps toward building healthier patterns", 5, "→", "ic_first_momentum", MilestoneType.PROGRESS),
    Milestone("Pattern Shifter", "Develop awareness of your triggers and responses", 10, "○", "ic_pattern_shifter", MilestoneType.PROGRESS),
    Milestone("Repair in Motion", "Learn to recover quickly when things don't go as planned", 3, "↑", "ic_repair_in_motion", MilestoneType.PROGRESS),
    Milestone("Wave Rider", "Master surfing through urges without acting on them", 7, "~", "ic_wave_rider", MilestoneType.PROGRESS),
    Milestone("Choice Maker", "Build consistent habits of choosing mindful alternatives", 25, "◆", "ic_choice_maker", MilestoneType.PROGRESS),
    Milestone("Strong Week", "Demonstrate sustained commitment to your wellbeing", 7, "▲", "ic_strong_week", MilestoneType.PROGRESS),
    Milestone("Momentum Build", "Create lasting change through consistent daily actions", 50, "■", "ic_momentum_build", MilestoneType.PROGRESS),
    Milestone("Pattern Master", "Develop deep insights into your behavioral patterns", 90, "◉", "ic_pattern_master", MilestoneType.PROGRESS),
    Milestone("Choice Champion", "Achieve mastery in making values-aligned decisions", 100, "●", "ic_choice_champion", MilestoneType.PROGRESS),
    
    // Avoidance Milestones
    Milestone("Avoidance Breaker", "Break free from procrastination and take your first action", 1, "🔗", "ic_avoidance_breaker", MilestoneType.AVOIDANCE),
    Milestone("Backlog Buster", "Tackle tasks you've been putting off with confidence", 5, "📋", "ic_backlog_buster", MilestoneType.AVOIDANCE),
    Milestone("Proactive Player", "Turn avoidance into proactive momentum in your life", 10, "➡️", "ic_proactive_player", MilestoneType.AVOIDANCE),
    Milestone("Admin Tamer", "Master the art of handling life's necessary tasks", 25, "✅", "ic_admin_tamer", MilestoneType.AVOIDANCE),
    Milestone("Life in Flow", "Create effortless productivity by eliminating avoidance", 50, "🌊", "ic_life_in_flow", MilestoneType.AVOIDANCE)
  )
  }
}