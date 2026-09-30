package com.androidengineers.agent_quickstart_android.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.androidengineers.agent_quickstart_android.data.history.HistoryEntity
import com.androidengineers.agent_quickstart_android.data.notes.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray

class VoxPilotDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    private val _notesFlow = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notesFlow: Flow<List<NoteEntity>> = _notesFlow.asStateFlow()

    private val _historyFlow = MutableStateFlow<List<HistoryEntity>>(emptyList())
    val historyFlow: Flow<List<HistoryEntity>> = _historyFlow.asStateFlow()

    init {
        // Initial load
        refreshNotes()
        refreshHistory()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                summary TEXT NOT NULL,
                key_points TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                date_millis INTEGER NOT NULL,
                duration_seconds INTEGER NOT NULL,
                preview TEXT NOT NULL,
                transcript_json TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS notes")
        db.execSQL("DROP TABLE IF EXISTS history")
        onCreate(db)
    }

    // --- Notes Operations ---

    suspend fun insertNote(note: NoteEntity): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", note.title)
            put("summary", note.summary)
            put("key_points", JSONArray(note.keyPoints).toString())
            put("created_at", note.createdAt)
            put("updated_at", note.updatedAt)
        }
        val rowId = db.insert("notes", null, values)
        refreshNotes()
        rowId
    }

    suspend fun deleteNote(id: Long): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val deleted = db.delete("notes", "id = ?", arrayOf(id.toString())) > 0
        refreshNotes()
        deleted
    }

    suspend fun getAllNotes(): List<NoteEntity> = withContext(Dispatchers.IO) {
        queryAllNotes()
    }

    private fun refreshNotes() {
        try {
            _notesFlow.value = queryAllNotes()
        } catch (_: Exception) {
            // Database might still be initializing
        }
    }

    private fun queryAllNotes(): List<NoteEntity> {
        val list = mutableListOf<NoteEntity>()
        val db = readableDatabase
        val cursor = db.query(
            "notes",
            null,
            null,
            null,
            null,
            null,
            "created_at DESC"
        )
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val titleIdx = it.getColumnIndexOrThrow("title")
            val summaryIdx = it.getColumnIndexOrThrow("summary")
            val keyPointsIdx = it.getColumnIndexOrThrow("key_points")
            val createdIdx = it.getColumnIndexOrThrow("created_at")
            val updatedIdx = it.getColumnIndexOrThrow("updated_at")

            while (it.moveToNext()) {
                val keyPointsStr = it.getString(keyPointsIdx)
                val keyPoints = parseJsonList(keyPointsStr)
                list.add(
                    NoteEntity(
                        id = it.getLong(idIdx),
                        title = it.getString(titleIdx),
                        summary = it.getString(summaryIdx),
                        keyPoints = keyPoints,
                        createdAt = it.getLong(createdIdx),
                        updatedAt = it.getLong(updatedIdx),
                    )
                )
            }
        }
        return list
    }

    // --- History Operations ---

    suspend fun insertHistory(history: HistoryEntity): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", history.title)
            put("date_millis", history.dateMillis)
            put("duration_seconds", history.durationSeconds)
            put("preview", history.preview)
            put("transcript_json", history.transcriptJson)
        }
        val rowId = db.insert("history", null, values)
        refreshHistory()
        rowId
    }

    suspend fun deleteHistory(id: Long): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val deleted = db.delete("history", "id = ?", arrayOf(id.toString())) > 0
        refreshHistory()
        deleted
    }

    suspend fun clearAllHistory(): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete("history", null, null)
        refreshHistory()
        true
    }

    suspend fun getAllHistory(): List<HistoryEntity> = withContext(Dispatchers.IO) {
        queryAllHistory()
    }

    private fun refreshHistory() {
        try {
            _historyFlow.value = queryAllHistory()
        } catch (_: Exception) {
            // Database might still be initializing
        }
    }

    private fun queryAllHistory(): List<HistoryEntity> {
        val list = mutableListOf<HistoryEntity>()
        val db = readableDatabase
        val cursor = db.query(
            "history",
            null,
            null,
            null,
            null,
            null,
            "date_millis DESC"
        )
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("id")
            val titleIdx = it.getColumnIndexOrThrow("title")
            val dateIdx = it.getColumnIndexOrThrow("date_millis")
            val durationIdx = it.getColumnIndexOrThrow("duration_seconds")
            val previewIdx = it.getColumnIndexOrThrow("preview")
            val transcriptIdx = it.getColumnIndexOrThrow("transcript_json")

            while (it.moveToNext()) {
                list.add(
                    HistoryEntity(
                        id = it.getLong(idIdx),
                        title = it.getString(titleIdx),
                        dateMillis = it.getLong(dateIdx),
                        durationSeconds = it.getLong(durationIdx),
                        preview = it.getString(previewIdx),
                        transcriptJson = it.getString(transcriptIdx),
                    )
                )
            }
        }
        return list
    }

    private fun parseJsonList(jsonStr: String): List<String> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val DATABASE_NAME = "voxpilot.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var instance: VoxPilotDatabase? = null

        fun getInstance(context: Context): VoxPilotDatabase {
            return instance ?: synchronized(this) {
                instance ?: VoxPilotDatabase(context.applicationContext).also { instance = it }
            }
        }
    }
}
