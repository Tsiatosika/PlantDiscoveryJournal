package com.example.plantdiscoveryjournal.data.local.dao

import androidx.room.*
import com.example.plantdiscoveryjournal.data.local.entity.DiscoveryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object pour les opérations sur les découvertes
 */
@Dao
interface DiscoveryDao {

    @Query("SELECT * FROM discoveries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllDiscoveriesByUser(userId: String): Flow<List<DiscoveryEntity>>

    @Query("SELECT * FROM discoveries WHERE id = :discoveryId")
    suspend fun getDiscoveryById(discoveryId: Long): DiscoveryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscovery(discovery: DiscoveryEntity): Long

    @Update
    suspend fun updateDiscovery(discovery: DiscoveryEntity)

    @Delete
    suspend fun deleteDiscovery(discovery: DiscoveryEntity)

    @Query("DELETE FROM discoveries WHERE id = :discoveryId")
    suspend fun deleteDiscoveryById(discoveryId: Long)

    @Query("DELETE FROM discoveries WHERE userId = :userId")
    suspend fun deleteAllDiscoveriesByUser(userId: String)
}