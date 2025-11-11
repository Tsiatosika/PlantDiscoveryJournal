package com.example.plantdiscoveryjournal.data.local.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.plantdiscoveryjournal.data.local.dao.DiscoveryDao
import com.example.plantdiscoveryjournal.data.local.entity.DiscoveryEntity

/**
 * Base de données Room pour l'application
 */
@Database(
    entities = [DiscoveryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun discoveryDao(): DiscoveryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plant_discovery_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}