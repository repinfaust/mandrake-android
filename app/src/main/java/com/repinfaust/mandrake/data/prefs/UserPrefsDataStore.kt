package com.repinfaust.mandrake.data.prefs
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import kotlinx.coroutines.flow.map
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler

class UserPrefsDataStore private constructor(private val context: Context) {
  private val store: DataStore<UserPrefs> = DataStoreFactory.create(
    serializer = UserPrefsSerializer,
    produceFile = { context.dataStoreFile("user_prefs.pb") }
  )
  
  companion object {
    @Volatile
    private var INSTANCE: UserPrefsDataStore? = null
    
    fun getInstance(context: Context): UserPrefsDataStore {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: UserPrefsDataStore(context.applicationContext).also { INSTANCE = it }
      }
    }
  }
  val points = store.data.map { it.points }
  val onboardingCompleted = store.data.map { it.onboardingCompleted }
  val supportRegion = store.data.map { it.supportRegion.ifEmpty { "UK" } }
  
  suspend fun addPoints(delta: Int) {
    store.updateData { it.toBuilder().setPoints((it.points + delta).coerceAtLeast(0)).build() }
  }
  
  suspend fun setOnboardingCompleted(completed: Boolean) {
    store.updateData { it.toBuilder().setOnboardingCompleted(completed).build() }
  }
  
  suspend fun setSupportRegion(region: String) {
    store.updateData { it.toBuilder().setSupportRegion(region).build() }
  }
}
