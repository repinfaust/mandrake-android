package com.repinfaust.mandrake.data.db
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.repinfaust.mandrake.data.dao.UrgeEventDao
import com.repinfaust.mandrake.data.dao.ScreeningDao
import com.repinfaust.mandrake.data.dao.RiskAssessmentDao
import com.repinfaust.mandrake.data.dao.RewardsDao
import com.repinfaust.mandrake.data.dao.RedemptionsDao
import com.repinfaust.mandrake.data.dao.CustomChipDao
import com.repinfaust.mandrake.data.entity.UrgeEvent
import com.repinfaust.mandrake.data.entity.ScreeningResult
import com.repinfaust.mandrake.data.entity.RiskAssessment
import com.repinfaust.mandrake.data.entity.Reward
import com.repinfaust.mandrake.data.entity.Redemption
import com.repinfaust.mandrake.data.entity.CustomChip

@Database(
    entities = [UrgeEvent::class, ScreeningResult::class, RiskAssessment::class, Reward::class, Redemption::class, CustomChip::class], 
    version = 10, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
  abstract fun urgeEventDao(): UrgeEventDao
  abstract fun screeningDao(): ScreeningDao
  abstract fun riskAssessmentDao(): RiskAssessmentDao
  abstract fun rewardsDao(): RewardsDao
  abstract fun redemptionsDao(): RedemptionsDao
  abstract fun customChipDao(): CustomChipDao
  
  companion object {
    @Volatile private var INSTANCE: AppDatabase? = null
    
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS screening_results (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    category TEXT NOT NULL,
                    responses TEXT NOT NULL,
                    band TEXT NOT NULL,
                    isSkipped INTEGER NOT NULL DEFAULT 0
                )
            """)
            
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS risk_assessments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    screenerBand TEXT NOT NULL,
                    urges7d INTEGER NOT NULL,
                    highIntensity7d INTEGER NOT NULL,
                    actedVsAlt14d REAL NOT NULL,
                    nightEpisodes7d INTEGER NOT NULL,
                    redFlags TEXT NOT NULL,
                    nudgeTier TEXT NOT NULL,
                    ruleTriggered TEXT
                )
            """)
        }
    }
    
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS rewards (
                    id TEXT PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL,
                    notes TEXT,
                    costPoints INTEGER,
                    template INTEGER NOT NULL DEFAULT 0,
                    active INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL
                )
            """)
            
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS redemptions (
                    id TEXT PRIMARY KEY NOT NULL,
                    rewardId TEXT NOT NULL,
                    eventRef TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    claimSource TEXT NOT NULL
                )
            """)
        }
    }
    
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                ALTER TABLE urge_events ADD COLUMN urgeAbout TEXT DEFAULT NULL
            """)
        }
    }
    
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                ALTER TABLE urge_events ADD COLUMN eventType TEXT NOT NULL DEFAULT 'BYPASSED_URGE'
            """)
            
            // Update existing records based on legacy gaveIn field
            database.execSQL("""
                UPDATE urge_events SET eventType = 'WENT_WITH_URGE' WHERE gaveIn = 1
            """)
            
            database.execSQL("""
                UPDATE urge_events SET eventType = 'AVOIDED_TASK' WHERE urgeAbout = 'Avoid task' AND gaveIn = 0
            """)
        }
    }
    
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                ALTER TABLE rewards ADD COLUMN requiredMilestoneCount INTEGER NOT NULL DEFAULT 1
            """)
            database.execSQL("""
                ALTER TABLE rewards ADD COLUMN specificMilestoneId TEXT DEFAULT NULL
            """)
            database.execSQL("""
                ALTER TABLE rewards ADD COLUMN milestoneType TEXT DEFAULT NULL
            """)
        }
    }
    
    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                ALTER TABLE urge_events ADD COLUMN customTactic TEXT DEFAULT NULL
            """)
        }
    }
    
    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                ALTER TABLE urge_events ADD COLUMN customUrgeAbout TEXT DEFAULT NULL
            """)
        }
    }
    
    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS custom_chips (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    text TEXT NOT NULL,
                    chipType TEXT NOT NULL,
                    usageCount INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1
                )
            """)
        }
    }
    
    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                ALTER TABLE custom_chips ADD COLUMN actionCategory TEXT DEFAULT NULL
            """)
            database.execSQL("""
                ALTER TABLE custom_chips ADD COLUMN actionMinutes INTEGER DEFAULT NULL
            """)
        }
    }
    
    fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
      Room.databaseBuilder(context, AppDatabase::class.java, "urge.db")
          .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
          .build().also { INSTANCE = it }
    }
  }
}
