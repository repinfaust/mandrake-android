package com.repinfaust.mandrake.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class TacticType { WALK, SMOKING, CALL, MUSIC, JOT, SHOWER, BREATH, OTHER }
enum class Mood { VERY_BAD, BAD, OK, GOOD, GREAT }
enum class TriggerType { STRESS, BOREDOM, SOCIAL, ANXIETY, FATIGUE, LONELY, HABIT, MONEY, ANGER, OTHER }

@Entity(tableName = "urge_events")
data class UrgeEvent(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long = Instant.now().toEpochMilli(),
  val eventType: EventType,
  val tactic: TacticType? = null,
  val customTactic: String? = null, // Custom text when tactic is OTHER
  val mood: Mood? = null,
  val usedWaveTimer: Boolean = false,
  val durationSeconds: Int = 0,
  val intensity: Int = 0,
  val trigger: TriggerType? = null,
  val urgeAbout: String? = null,
  val customUrgeAbout: String? = null, // Custom text when urgeAbout is "Other"
  // Legacy fields for migration compatibility
  val gaveIn: Boolean = false
)
