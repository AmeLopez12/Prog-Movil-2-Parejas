package com.example.sicenet.data.local

import androidx.room.*
import com.example.sicenet.data.model.Alumno
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumnoDao {
    @Query("SELECT * FROM alumno_perfil LIMIT 1")
    fun getAlumno(): Flow<Alumno?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlumno(alumno: Alumno)

    @Query("DELETE FROM alumno_perfil")
    suspend fun deleteAlumno()
}
