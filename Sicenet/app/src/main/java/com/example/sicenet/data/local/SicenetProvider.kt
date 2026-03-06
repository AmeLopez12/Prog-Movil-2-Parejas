package com.example.sicenet.data.local

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
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
        
        // Verificación de seguridad: ¿Hay una sesión activa?
        val prefs = context.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
        val cookie = prefs.getString("session_cookie", null)
        
        if (cookie == null) {
            return null
        }

        val db = database.openHelper.readableDatabase
        val tableName = getTableName(uri) ?: return null

        val query = SupportSQLiteQueryBuilder.builder(tableName)
            .columns(projection)
            .selection(selection, selectionArgs)
            .orderBy(sortOrder)
            .create()

        val cursor = db.query(query)
        cursor.setNotificationUri(context.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) return null
        
        val context = context ?: return null
        val db = database.openHelper.writableDatabase
        val tableName = getTableName(uri) ?: return null

        // Uso de CONFLICT_REPLACE para manejar duplicados
        val id = db.insert(tableName, SQLiteDatabase.CONFLICT_REPLACE, values)
        
        if (id > 0) {
            // Notificar cambio en la URI base
            context.contentResolver.notifyChange(uri, null)
            return ContentUris.withAppendedId(uri, id)
        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val context = context ?: return 0
        val db = database.openHelper.writableDatabase
        val tableName = getTableName(uri) ?: return 0

        val count = db.delete(tableName, selection, selectionArgs)
        if (count > 0) {
            // Notificar cambio en la URI base
            context.contentResolver.notifyChange(uri, null)
        }
        return count
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        if (values == null) return 0

        val context = context ?: return 0
        val db = database.openHelper.writableDatabase
        val tableName = getTableName(uri) ?: return 0

        // Uso de CONFLICT_REPLACE para manejar duplicados durante la actualización
        val count = db.update(tableName, SQLiteDatabase.CONFLICT_REPLACE, values, selection, selectionArgs)
        if (count > 0) {
            // Notificar cambio en la URI base
            context.contentResolver.notifyChange(uri, null)
        }
        return count
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            MATERIAS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.materias"
            KARDEX -> "vnd.android.cursor.dir/vnd.$AUTHORITY.kardex"
            else -> null
        }
    }

    private fun getTableName(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            MATERIAS -> "materias"
            KARDEX -> "kardex"
            else -> null
        }
    }
}
