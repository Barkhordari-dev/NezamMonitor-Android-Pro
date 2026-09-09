package com.nezammonitor.app.data

import android.content.Context
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CaseEntity::class, SpecificationEntity::class, EngineerEntity::class, FeeEntity::class, ReportEntity::class, FollowUpEditEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NezamDatabase : RoomDatabase() {
    abstract fun nezamDao(): NezamDao

    // Make INSTANCE public
    companion object {
        @Volatile
        var INSTANCE: NezamDatabase? = null
            private set

        fun getDatabase(context: Context): NezamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NezamDatabase::class.java,
                    "nezam_monitor.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}