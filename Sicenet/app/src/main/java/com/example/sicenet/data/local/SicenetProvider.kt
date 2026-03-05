package com.example.sicenet.data.local

import android.content.Context
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

class SicenetProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.sicenet.provider"
        
        const val MATERIAS = 1
        const val KARDEX = 2

        val URI_MATERIAS: Uri = Uri.parse("content://$AUTHORITY/materias")
        val URI_KARDEX: Uri = Uri.parse("content://$AUTHORITY/kardex")

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "materias", MATERIAS)
            addURI(AUTHORITY, "kardex", KARDEX)
        }
    }

    private lateinit var database: SicenetDatabase

    override fun onCreate(): Boolean {
        database = SicenetDatabase.getDatabase(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val context = context ?: return null
        
        // Verificación de seguridad: ¿Hay una sesión activa?
        val prefs = context.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val cookie = prefs.getString("session_cookie", null)
        
        if (cookie == null) {
            // Si no hay cookie de sesión, no entregamos ningún dato al exterior
            return null
        }

        val db = database.openHelper.readableDatabase
        return when (uriMatcher.match(uri)) {
            MATERIAS -> {
                db.query("SELECT * FROM materias")
            }
            KARDEX -> {
                db.query("SELECT * FROM kardex")
            }
            else -> null
        }
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
