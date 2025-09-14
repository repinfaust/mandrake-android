package com.repinfaust.mandrake.data.repo

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.repinfaust.mandrake.data.entity.UrgeEvent
import com.repinfaust.mandrake.data.entity.EventType
import kotlinx.coroutines.tasks.await

class FirestoreUrgeRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    private fun getUserCollection() = 
        currentUserId?.let { userId ->
            firestore.collection("users").document(userId).collection("urgeEvents")
        }
    
    suspend fun logEvent(event: UrgeEvent): Result<Unit> {
        return try {
            Log.d("MandrakeFirestore", "logEvent called with currentUserId: $currentUserId")
            val collection = getUserCollection() 
                ?: return Result.failure(Exception("User not authenticated"))
            
            val eventData = hashMapOf(
                "timestamp" to event.timestamp,
                "type" to event.eventType.name,
                "tactic" to event.tactic?.name,
                "customTactic" to event.customTactic,
                "mood" to event.mood?.name,
                "usedWaveTimer" to event.usedWaveTimer,
                "durationSeconds" to event.durationSeconds,
                "intensity" to event.intensity,
                "trigger" to event.trigger?.name,
                "urgeAbout" to event.urgeAbout,
                "customUrgeAbout" to event.customUrgeAbout,
                "gaveIn" to event.gaveIn
            )
            
            Log.d("MandrakeFirestore", "Writing event data to Firestore: $eventData")
            collection.add(eventData).await()
            Log.d("MandrakeFirestore", "Event successfully written to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MandrakeFirestore", "Failed to write event to Firestore", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteAllEvents(): Result<Unit> {
        return try {
            Log.d("MandrakeFirestore", "deleteAllEvents called with currentUserId: $currentUserId")
            val collection = getUserCollection() 
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Get all documents and delete them
            val snapshot = collection.get().await()
            Log.d("MandrakeFirestore", "Found ${snapshot.documents.size} documents to delete")
            
            // Delete all documents in batches
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            Log.d("MandrakeFirestore", "Successfully deleted all events")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MandrakeFirestore", "Failed to delete events from Firestore", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAllEvents(): List<UrgeEvent> {
        return try {
            val collection = getUserCollection() ?: return emptyList()
            
            val snapshot = collection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    UrgeEvent(
                        id = 0,
                        timestamp = doc.getLong("timestamp") ?: 0,
                        eventType = doc.getString("type")?.let { EventType.valueOf(it) } ?: EventType.BYPASSED_URGE,
                        tactic = doc.getString("tactic")?.let { 
                            com.repinfaust.mandrake.data.entity.TacticType.valueOf(it) 
                        },
                        customTactic = doc.getString("customTactic"),
                        mood = doc.getString("mood")?.let { 
                            com.repinfaust.mandrake.data.entity.Mood.valueOf(it) 
                        },
                        usedWaveTimer = doc.getBoolean("usedWaveTimer") ?: false,
                        durationSeconds = doc.getLong("durationSeconds")?.toInt() ?: 0,
                        intensity = doc.getLong("intensity")?.toInt() ?: 0,
                        trigger = doc.getString("trigger")?.let { 
                            com.repinfaust.mandrake.data.entity.TriggerType.valueOf(it) 
                        },
                        urgeAbout = doc.getString("urgeAbout"),
                        customUrgeAbout = doc.getString("customUrgeAbout"),
                        gaveIn = doc.getBoolean("gaveIn") ?: false
                    )
                } catch (e: Exception) {
                    null // Skip malformed documents
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getEventsInRange(startTime: Long, endTime: Long): List<UrgeEvent> {
        return try {
            val collection = getUserCollection() ?: return emptyList()
            
            val snapshot = collection
                .whereGreaterThanOrEqualTo("timestamp", startTime)
                .whereLessThanOrEqualTo("timestamp", endTime)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    UrgeEvent(
                        id = 0,
                        timestamp = doc.getLong("timestamp") ?: 0,
                        eventType = doc.getString("type")?.let { EventType.valueOf(it) } ?: EventType.BYPASSED_URGE,
                        tactic = doc.getString("tactic")?.let { 
                            com.repinfaust.mandrake.data.entity.TacticType.valueOf(it) 
                        },
                        customTactic = doc.getString("customTactic"),
                        mood = doc.getString("mood")?.let { 
                            com.repinfaust.mandrake.data.entity.Mood.valueOf(it) 
                        },
                        usedWaveTimer = doc.getBoolean("usedWaveTimer") ?: false,
                        durationSeconds = doc.getLong("durationSeconds")?.toInt() ?: 0,
                        intensity = doc.getLong("intensity")?.toInt() ?: 0,
                        trigger = doc.getString("trigger")?.let { 
                            com.repinfaust.mandrake.data.entity.TriggerType.valueOf(it) 
                        },
                        urgeAbout = doc.getString("urgeAbout"),
                        customUrgeAbout = doc.getString("customUrgeAbout"),
                        gaveIn = doc.getBoolean("gaveIn") ?: false
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}