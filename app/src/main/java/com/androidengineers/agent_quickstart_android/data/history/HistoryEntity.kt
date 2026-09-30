package com.androidengineers.agent_quickstart_android.data.history

data class HistoryEntity(
    val id: Long = 0L,
    val title: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0L,
    val preview: String = "",
    val transcriptJson: String = "",
)
