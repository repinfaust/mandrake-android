package com.repinfaust.mandrake.domain
import com.repinfaust.mandrake.data.entity.UrgeEvent

object SignpostRules {
  data class Result(val soft: Boolean, val firm: Boolean)
  fun evaluate(events: List<UrgeEvent>): Result {
    val last20 = events.take(20)
    val highIntensityCluster = last20.count { it.intensity >= 7 } >= 5
    val failedAfterLongTimer = last20.count { it.gaveIn && it.durationSeconds >= 900 } >= 3
    val lateNightNoAction = last20.count { !it.usedWaveTimer && hourOf(it) in 0..4 } >= 4
    val soft = highIntensityCluster || lateNightNoAction
    val firm = soft && failedAfterLongTimer
    return Result(soft, firm)
  }
  private fun hourOf(e: UrgeEvent): Int = java.time.Instant.ofEpochMilli(e.timestamp)
    .atZone(java.time.ZoneId.systemDefault()).hour
}
