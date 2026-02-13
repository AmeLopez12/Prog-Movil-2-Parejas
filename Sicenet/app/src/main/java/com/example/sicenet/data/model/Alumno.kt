package com.example.sicenet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "alumno_perfil")
data class Alumno(
    @PrimaryKey val matricula: String = "",
    val nombre: String = "",
    val carrera: String = "",
    val especialidad: String = "",
    val semActual: Int = 0,
    val cdtosAcumulados: Int = 0,
    val cdtosActuales: Int = 0,
    val estatus: String = "",
    val inscrito: Boolean = false,
    val fechaReins: String = "",
    val modEducativo: Int = 0,
    val adeudo: Boolean = false,
    val adeudoDescripcion: String = "",
    val urlFoto: String = "",
    val lineamiento: Int = 0,
    val promedioGeneral: String = "N/A",
    val lastUpdate: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromJson(jsonString: String): Alumno {
            return try {
                val json = JSONObject(jsonString)
                Alumno(
                    nombre = json.optString("nombre"),
                    matricula = json.optString("matricula"),
                    carrera = json.optString("carrera"),
                    especialidad = json.optString("especialidad"),
                    semActual = json.optInt("semActual"),
                    cdtosAcumulados = json.optInt("cdtosAcumulados"),
                    cdtosActuales = json.optInt("cdtosActuales"),
                    estatus = json.optString("estatus"),
                    inscrito = json.optBoolean("inscrito"),
                    fechaReins = json.optString("fechaReins"),
                    modEducativo = json.optInt("modEducativo"),
                    adeudo = json.optBoolean("adeudo"),
                    adeudoDescripcion = json.optString("adeudoDescripcion"),
                    urlFoto = json.optString("urlFoto"),
                    lineamiento = json.optInt("lineamiento")
                )
            } catch (e: Exception) {
                Alumno()
            }
        }
    }
}
