package com.example.sicenet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "materias")
data class Materia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clave: String = "",
    val nombre: String = "",
    val grupo: String = "",
    val docente: String = "",
    val creditos: Int = 0,
    val lunes: String = "",
    val martes: String = "",
    val miercoles: String = "",
    val jueves: String = "",
    val viernes: String = "",
    val sabado: String = "",
    val domingo: String = "",
    val aula: String = "",
    val lastUpdate: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromJsonList(jsonString: String): List<Materia> {
            val list = mutableListOf<Materia>()
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val json = array.getJSONObject(i)
                    list.add(
                        Materia(
                            clave = json.optString("clvOficial"),
                            nombre = json.optString("Materia"),
                            grupo = json.optString("Grupo"),
                            docente = json.optString("Docente"),
                            creditos = json.optInt("CreditosMateria"),
                            lunes = json.optString("Lunes"),
                            martes = json.optString("Martes"),
                            miercoles = json.optString("Miercoles"),
                            jueves = json.optString("Jueves"),
                            viernes = json.optString("Viernes"),
                            sabado = json.optString("Sabado"),
                            domingo = json.optString("Domingo"),
                            aula = "" // El aula viene dentro de las cadenas de los días en este JSON
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
    }
}
