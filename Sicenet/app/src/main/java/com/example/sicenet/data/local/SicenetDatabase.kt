package com.example.sicenet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sicenet.data.model.Alumno
import com.example.sicenet.data.model.Materia

@Database(entities = [Alumno::class, Materia::class], version = 1, exportSchema = false)
abstract class SicenetDatabase : RoomDatabase() {
    abstract fun alumnoDao(): AlumnoDao
    abstract fun materiaDao(): MateriaDao

    companion object {
        @Volatile
        private var INSTANCE: SicenetDatabase? = null

        fun getDatabase(context: Context): SicenetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SicenetDatabase::class.java,
                    "sicenet_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
