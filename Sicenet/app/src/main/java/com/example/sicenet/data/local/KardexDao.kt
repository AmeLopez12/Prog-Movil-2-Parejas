package com.example.sicenet.data.local

import androidx.room.*
import com.example.sicenet.data.model.Kardex
import kotlinx.coroutines.flow.Flow

@Dao
interface KardexDao {
    @Query("SELECT * FROM kardex")
    fun getAllKardex(): Flow<List<Kardex>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(kardex: List<Kardex>)

    @Query("DELETE FROM kardex")
    suspend fun deleteAll()

    @Query("SELECT MAX(lastUpdate) FROM kardex")
    suspend fun getLastUpdateTime(): Long?
}
