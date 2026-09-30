package com.androidengineers.agent_quickstart_android.data.notes

import android.content.Context
import com.androidengineers.agent_quickstart_android.data.VoxPilotDatabase
import com.androidengineers.agent_quickstart_android.model.TranscriptSpeaker
import com.androidengineers.agent_quickstart_android.model.TranscriptTurn
import kotlinx.coroutines.flow.Flow

class NotesRepository(context: Context) {
    private val database = VoxPilotDatabase.getInstance(context)

    val allNotes: Flow<List<NoteEntity>> = database.notesFlow

    suspend fun saveNote(note: NoteEntity): Long {
        return database.insertNote(note)
    }

    suspend fun deleteNote(id: Long): Boolean {
        return database.deleteNote(id)
    }

    suspend fun createNoteFromTranscripts(
        turns: List<TranscriptTurn>,
        customTitle: String? = null,
    ): NoteEntity? {
        if (turns.isEmpty()) return null

        val userTurns = turns.filter { it.speaker == TranscriptSpeaker.USER && it.text.isNotBlank() }
        val agentTurns = turns.filter { it.speaker == TranscriptSpeaker.AGENT && it.text.isNotBlank() }

        if (userTurns.isEmpty() && agentTurns.isEmpty()) return null

        // Derive title
        val title = customTitle ?: when {
            userTurns.isNotEmpty() -> {
                val firstQuery = userTurns.first().text.trim()
                if (firstQuery.length > 50) firstQuery.take(47) + "..." else firstQuery
            }
            else -> "Study Notes - ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
        }

        // Derive summary
        val summary = when {
            agentTurns.isNotEmpty() -> {
                agentTurns.joinToString(" ") { it.text.trim() }.take(300).let {
                    if (it.length >= 300) "$it..." else it
                }
            }
            else -> userTurns.joinToString(" ") { it.text.trim() }.take(200)
        }

        // Derive key points
        val keyPoints = mutableListOf<String>()
        turns.forEach { turn ->
            val speakerTag = if (turn.speaker == TranscriptSpeaker.USER) "Q" else "Key"
            val sentences = turn.text.split(Regex("[.!?]\\s+")).filter { it.isNotBlank() }
            sentences.take(2).forEach { s ->
                val cleaned = s.trim().trimEnd('.', '!', '?')
                if (cleaned.length in 10..150) {
                    keyPoints.add("[$speakerTag] $cleaned")
                }
            }
        }

        val note = NoteEntity(
            title = title.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() },
            summary = summary,
            keyPoints = keyPoints.distinct().take(6),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

        val rowId = database.insertNote(note)
        return if (rowId > 0) note.copy(id = rowId) else null
    }
}
