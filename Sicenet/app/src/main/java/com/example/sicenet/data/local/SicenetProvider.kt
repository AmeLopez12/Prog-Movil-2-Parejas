package com.example.sicenet.data.local

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteQueryBuilder

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
        
        // Verificación de seguridad adicional: ¿Hay una sesión activa?
        val prefs = context.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val cookie = prefs.getString("session_cookie", null)
        
        if (cookie == null) {
            // Si no hay sesión, no permitimos el acceso aunque tenga el permiso de lectura
            return null
        }

        val db = database.openHelper.readableDatabase
        val tableName = when (uriMatcher.match(uri)) {
            MATERIAS -> "materias"
            KARDEX -> "kardex"
            else -> return null
        }

        // Usamos SupportSQLiteQueryBuilder para construir la consulta dinámicamente
        val query = SupportSQLiteQueryBuilder.builder(tableName)
            .columns(projection)
            .selection(selection, selectionArgs)
            .orderBy(sortOrder)
            .create()

        return db.query(query)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // En este proyecto, los datos se sincronizan vía Workers. 
        // El Provider es principalmente para lectura externa.
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            MATERIAS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.materias"
            KARDEX -> "vnd.android.cursor.dir/vnd.$AUTHORITY.kardex"
            else -> null
        }
    }
}
