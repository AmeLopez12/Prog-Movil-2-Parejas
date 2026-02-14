package com.example.sicenet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sicenet.data.model.*

@Database(
    entities = [Alumno::class, Materia::class, Kardex::class, CalifUnidad::class, CalifFinal::class], 
    version = 4, 
    exportSchema = false
)
abstract class SicenetDatabase : RoomDatabase() {
    abstract fun alumnoDao(): AlumnoDao
    abstract fun materiaDao(): MateriaDao
    abstract fun kardexDao(): KardexDao
    abstract fun califUnidadDao(): CalifUnidadDao
    abstract fun califFinalDao(): CalifFinalDao

    companion object {
        @Volatile
        private var INSTANCE: SicenetDatabase? = null

        fun getDatabase(context: Context): SicenetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SicenetDatabase::class.java,
                    "sicenet_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
