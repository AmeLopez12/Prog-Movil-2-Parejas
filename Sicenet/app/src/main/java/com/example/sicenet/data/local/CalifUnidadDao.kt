package com.example.sicenet.data.local

import androidx.room.*
import com.example.sicenet.data.model.CalifUnidad
import kotlinx.coroutines.flow.Flow

@Dao
interface CalifUnidadDao {
    @Query("SELECT * FROM calif_unidades")
    fun getAllCalifUnidades(): Flow<List<CalifUnidad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(califUnidades: List<CalifUnidad>)

    @Query("DELETE FROM calif_unidades")
    suspend fun deleteAll()

    @Query("SELECT MAX(lastUpdate) FROM calif_unidades")
    suspend fun getLastUpdateTime(): Long?
}
