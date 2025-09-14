package com.repinfaust.mandrake.data.db
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.repinfaust.mandrake.data.entity.*

class Converters {
  private val gson = Gson()
  
  @TypeConverter fun fromTactic(t: TacticType) = t.name
  @TypeConverter fun toTactic(s: String) = TacticType.valueOf(s)
  @TypeConverter fun fromMood(m: Mood) = m.name
  @TypeConverter fun toMood(s: String) = Mood.valueOf(s)
  @TypeConverter fun fromChipType(t: ChipType) = t.name
  @TypeConverter fun toChipType(s: String) = ChipType.valueOf(s)
  
  // New converters for screening and risk assessment
  @TypeConverter fun fromRiskBand(band: RiskBand) = band.name
  @TypeConverter fun toRiskBand(s: String) = RiskBand.valueOf(s)
  
  @TypeConverter fun fromScreeningType(type: ScreeningType) = type.name
  @TypeConverter fun toScreeningType(s: String) = ScreeningType.valueOf(s)
  
  @TypeConverter fun fromNudgeTier(tier: NudgeTier) = tier.name
  @TypeConverter fun toNudgeTier(s: String) = NudgeTier.valueOf(s)
  
  @TypeConverter 
  fun fromIntList(list: List<Int>): String = gson.toJson(list)
  @TypeConverter 
  fun toIntList(json: String): List<Int> = gson.fromJson(json, object : TypeToken<List<Int>>() {}.type)
  
  @TypeConverter
  fun fromRedFlags(redFlags: RedFlags): String = gson.toJson(redFlags)
  @TypeConverter
  fun toRedFlags(json: String): RedFlags = gson.fromJson(json, RedFlags::class.java)
}
