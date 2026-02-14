package com.example.sicenet.data.local

import androidx.room.*
import com.example.sicenet.data.model.CalifFinal
import kotlinx.coroutines.flow.Flow

@Dao
interface CalifFinalDao {
    @Query("SELECT * FROM calif_final")
    fun getAllCalifFinal(): Flow<List<CalifFinal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(califFinal: List<CalifFinal>)

    @Query("DELETE FROM calif_final")
    suspend fun deleteAll()

    @Query("SELECT MAX(lastUpdate) FROM calif_final")
    suspend fun getLastUpdateTime(): Long?
}
