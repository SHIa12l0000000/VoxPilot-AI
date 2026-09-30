package com.androidengineers.agent_quickstart_android.data.history

import android.content.Context
import com.androidengineers.agent_quickstart_android.data.VoxPilotDatabase
import com.androidengineers.agent_quickstart_android.data.notes.NoteEntity
import com.androidengineers.agent_quickstart_android.data.notes.NotesRepository
import com.androidengineers.agent_quickstart_android.model.TranscriptSpeaker
import com.androidengineers.agent_quickstart_android.model.TranscriptTurn
import com.androidengineers.agent_quickstart_android.model.TranscriptTurnStatus
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class HistoryRepository(context: Context) {
    private val database = VoxPilotDatabase.getInstance(context)
    private val notesRepository = NotesRepository(context)

    val allHistory: Flow<List<HistoryEntity>> = database.historyFlow

    suspend fun saveSession(
        turns: List<TranscriptTurn>,
        durationSeconds: Long,
    ): Long {
        val validTurns = turns.filter { it.text.isNotBlank() }
        if (validTurns.isEmpty()) return -1L

        val firstUserTurn = validTurns.firstOrNull { it.speaker == TranscriptSpeaker.USER }
        val title = firstUserTurn?.text?.trim()?.take(50)
            ?: "Voice Session - ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"

        val preview = validTurns.takeLast(2).joinToString(" | ") {
            val speaker = if (it.speaker == TranscriptSpeaker.USER) "You" else "Vox"
            "$speaker: ${it.text.trim()}"
        }.take(160)

        // Serialize turns into JSON array (only text, speaker, and timestamp - never tokens or credentials)
        val jsonArray = JSONArray()
        validTurns.forEach { turn ->
            jsonArray.put(
                JSONObject().apply {
                    put("speaker", turn.speaker.name)
                    put("text", turn.text)
                    put("status", turn.status.name)
                    put("time", turn.createdAtMillis)
                }
            )
        }

        val history = HistoryEntity(
            title = title,
            dateMillis = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            preview = preview,
            transcriptJson = jsonArray.toString(),
        )

        return database.insertHistory(history)
    }

    suspend fun deleteHistory(id: Long): Boolean {
        return database.deleteHistory(id)
    }

    suspend fun clearAll(): Boolean {
        return database.clearAllHistory()
    }

    suspend fun convertToNote(history: HistoryEntity): NoteEntity? {
        val turns = parseTurnsFromJson(history.transcriptJson)
        return notesRepository.createNoteFromTranscripts(
            turns = turns,
            customTitle = "Note: ${history.title}",
        )
    }

    private fun parseTurnsFromJson(jsonStr: String): List<TranscriptTurn> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<TranscriptTurn>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val speaker = if (obj.optString("speaker") == "USER") TranscriptSpeaker.USER else TranscriptSpeaker.AGENT
                val text = obj.optString("text")
                list.add(
                    TranscriptTurn(
                        key = "hist_$i",
                        turnId = i.toLong(),
                        streamId = null,
                        speaker = speaker,
                        text = text,
                        status = TranscriptTurnStatus.END,
                        createdAtMillis = obj.optLong("time", System.currentTimeMillis()),
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
