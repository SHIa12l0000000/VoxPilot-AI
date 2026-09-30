package com.androidengineers.agent_quickstart_android.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.androidengineers.agent_quickstart_android.data.history.HistoryRepository
import com.androidengineers.agent_quickstart_android.data.notes.NoteEntity
import com.androidengineers.agent_quickstart_android.data.notes.NotesRepository
import com.androidengineers.agent_quickstart_android.model.TranscriptSpeaker
import com.androidengineers.agent_quickstart_android.model.TranscriptTurn
import com.androidengineers.agent_quickstart_android.model.TranscriptTurnStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotesAndHistoryTest {
    private lateinit var context: Context
    private lateinit var notesRepository: NotesRepository
    private lateinit var historyRepository: HistoryRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notesRepository = NotesRepository(context)
        historyRepository = HistoryRepository(context)
    }

    @Test
    fun createNoteFromTranscriptsExtractsTitleAndKeyPoints() = runBlocking {
        val turns = listOf(
            TranscriptTurn(
                key = "turn_1",
                turnId = 1L,
                streamId = null,
                speaker = TranscriptSpeaker.USER,
                text = "Explain binary search in Hindi",
                status = TranscriptTurnStatus.END,
                createdAtMillis = 1000L,
            ),
            TranscriptTurn(
                key = "turn_2",
                turnId = 2L,
                streamId = null,
                speaker = TranscriptSpeaker.AGENT,
                text = "Binary search works by dividing sorted array in halves. It is logarithmic.",
                status = TranscriptTurnStatus.END,
                createdAtMillis = 2000L,
            ),
        )

        val note = notesRepository.createNoteFromTranscripts(turns)
        assertNotNull(note)
        assertTrue(note!!.id > 0)
        assertTrue(note.title.contains("Explain binary search", ignoreCase = true))
        assertTrue(note.summary.contains("Binary search"))
        assertTrue(note.keyPoints.isNotEmpty())

        val notes = notesRepository.allNotes.first()
        assertTrue(notes.any { it.id == note.id })
    }

    @Test
    fun historySavingAndConversionToNoteWorks() = runBlocking {
        val turns = listOf(
            TranscriptTurn(
                key = "turn_1",
                turnId = 1L,
                streamId = null,
                speaker = TranscriptSpeaker.USER,
                text = "Cricket analogy for sorting",
                status = TranscriptTurnStatus.END,
                createdAtMillis = 1000L,
            ),
            TranscriptTurn(
                key = "turn_2",
                turnId = 2L,
                streamId = null,
                speaker = TranscriptSpeaker.AGENT,
                text = "Think of batting averages being sorted from lowest to highest.",
                status = TranscriptTurnStatus.END,
                createdAtMillis = 2000L,
            ),
        )

        val historyId = historyRepository.saveSession(turns, 45L)
        assertTrue(historyId > 0)

        val historyList = historyRepository.allHistory.first()
        val item = historyList.firstOrNull { it.id == historyId }
        assertNotNull(item)
        assertEquals(45L, item!!.durationSeconds)
        assertTrue(item.title.contains("Cricket analogy"))

        // Convert to note
        val convertedNote = historyRepository.convertToNote(item)
        assertNotNull(convertedNote)
        assertTrue(convertedNote!!.title.contains("Note:"))
    }
}
