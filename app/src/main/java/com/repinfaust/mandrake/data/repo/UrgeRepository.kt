package com.repinfaust.mandrake.data.repo
import android.content.Context
import com.repinfaust.mandrake.data.db.AppDatabase
import com.repinfaust.mandrake.data.entity.UrgeEvent
import kotlinx.coroutines.flow.Flow

class UrgeRepository(context: Context) {
  private val dao = AppDatabase.get(context).urgeEventDao()
  suspend fun logEvent(event: UrgeEvent) = dao.insert(event)
  fun recent(limit: Int = 100) = dao.recent(limit)
  fun between(from: Long, to: Long) = dao.between(from, to)
  fun count(): Flow<Int> = dao.count()
}
