package com.androidengineers.agent_quickstart_android.data.notes

data class NoteEntity(
    val id: Long = 0L,
    val title: String,
    val summary: String,
    val keyPoints: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
