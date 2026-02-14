package com.example.sicenet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "calif_unidades")
data class CalifUnidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String = "",
    val unidades: String = "", // Guardaremos las calificaciones como un string JSON o formateado
    val lastUpdate: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromJsonList(jsonString: String): List<CalifUnidad> {
            val list = mutableListOf<CalifUnidad>()
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val json = array.getJSONObject(i)
                    // Sicenet suele enviar unidades como campos individuales (C1, C2...) o una lista
                    // Guardamos todo el objeto de la materia para procesarlo en la UI
                    list.add(
                        CalifUnidad(
                            materia = json.optString("Materia"),
                            unidades = json.toString()
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
