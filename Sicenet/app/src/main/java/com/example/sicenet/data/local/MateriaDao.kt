package com.example.sicenet.data.local

import androidx.room.*
import com.example.sicenet.data.model.Materia
import kotlinx.coroutines.flow.Flow

@Dao
interface MateriaDao {
    @Query("SELECT * FROM materias")
    fun getAllMaterias(): Flow<List<Materia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materias: List<Materia>)

    @Query("DELETE FROM materias")
    suspend fun deleteAll()

    @Query("SELECT MAX(lastUpdate) FROM materias")
    suspend fun getLastUpdateTime(): Long?
}
