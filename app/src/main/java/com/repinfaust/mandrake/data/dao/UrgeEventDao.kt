package com.repinfaust.mandrake.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.repinfaust.mandrake.data.entity.UrgeEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface UrgeEventDao {
  @Insert
  suspend fun insert(event: UrgeEvent)

  @Query("SELECT * FROM urge_events ORDER BY timestamp DESC LIMIT :limit")
  fun recent(limit: Int = 100): Flow<List<UrgeEvent>>

  @Query("SELECT * FROM urge_events WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
  fun between(from: Long, to: Long): Flow<List<UrgeEvent>>

  @Query("SELECT COUNT(*) FROM urge_events")
  fun count(): Flow<Int>
}
