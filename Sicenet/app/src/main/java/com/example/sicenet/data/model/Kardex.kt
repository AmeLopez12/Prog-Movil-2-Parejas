package com.example.sicenet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "kardex")
data class Kardex(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clave: String = "",
    val materia: String = "",
    val calificacion: Int = 0,
    val periodo: String = "",
    val acreditacion: String = "",
    val lastUpdate: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromJsonList(jsonString: String): List<Kardex> {
            val list = mutableListOf<Kardex>()
            try {
                // El JSON viene envuelto en un objeto con la llave "lstKardex"
                val root = JSONObject(jsonString)
                val array = root.getJSONArray("lstKardex")
                
                for (i in 0 until array.length()) {
                    val json = array.getJSONObject(i)
                    
                    // Construir el periodo combinando P1 (Nombre) y A1 (Año)
                    val p1 = json.optString("P1")
                    val a1 = json.optString("A1")
                    val periodoCompleto = if (p1.isNotBlank() && a1.isNotBlank()) "$p1 $a1" else "Desconocido"

                    list.add(
                        Kardex(
                            clave = json.optString("ClvOfiMat"),
                            materia = json.optString("Materia"),
                            calificacion = json.optInt("Calif"),
                            periodo = periodoCompleto,
                            acreditacion = json.optString("Acred")
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
