package com.example.sicenet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "calif_final")
data class CalifFinal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String = "",
    val clave: String = "",
    val calificacion: Int = 0,
    val acreditacion: String = "",
    val lastUpdate: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromJsonList(jsonString: String): List<CalifFinal> {
            val list = mutableListOf<CalifFinal>()
            try {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val json = array.getJSONObject(i)
                    list.add(
                        CalifFinal(
                            materia = json.optString("materia"),
                            clave = json.optString("clvMat"),
                            calificacion = json.optInt("calif"),
                            acreditacion = json.optString("acreditacion")
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
