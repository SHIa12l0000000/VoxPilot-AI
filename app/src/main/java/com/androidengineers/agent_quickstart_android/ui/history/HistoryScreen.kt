package com.androidengineers.agent_quickstart_android.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidengineers.agent_quickstart_android.data.history.HistoryEntity
import com.androidengineers.agent_quickstart_android.ui.theme.VoxCoral
import com.androidengineers.agent_quickstart_android.ui.theme.VoxCyan
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyBorder
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyCard
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyDark
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavySurface
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextMuted
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextPrimary
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyList: List<HistoryEntity>,
    onDeleteHistory: (Long) -> Unit,
    onConvertToNote: (HistoryEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
    var selectedHistoryForDetails by remember { mutableStateOf<HistoryEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoxNavyDark)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(VoxCyan),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Session History",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = VoxTextPrimary,
            )
        }
        Text(
            text = "Completed voice conversations stored locally",
            fontSize = 12.sp,
            color = VoxTextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
        )

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = VoxNavyBorder,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No conversation history",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoxTextSecondary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When you finish speaking with VoxPilot, session logs will be archived here.",
                        fontSize = 13.sp,
                        color = VoxTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(historyList, key = { it.id }) { item ->
                    HistoryCard(
                        item = item,
                        dateStr = dateFormat.format(Date(item.dateMillis)),
                        onDelete = { onDeleteHistory(item.id) },
                        onConvertToNote = { onConvertToNote(item) },
                        onOpen = { selectedHistoryForDetails = item },
                    )
                }
            }
        }
    }

    // Detail dialog
    selectedHistoryForDetails?.let { history ->
        AlertDialog(
            onDismissRequest = { selectedHistoryForDetails = null },
            title = {
                Text(
                    text = history.title,
                    fontWeight = FontWeight.Bold,
                    color = VoxTextPrimary,
                )
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = VoxCyan,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${history.durationSeconds}s duration",
                            fontSize = 12.sp,
                            color = VoxTextMuted,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = history.preview,
                        fontSize = 14.sp,
                        color = VoxTextSecondary,
                        lineHeight = 20.sp,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConvertToNote(history)
                        selectedHistoryForDetails = null
                    },
                ) {
                    Text("Convert to Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedHistoryForDetails = null }) {
                    Text("Close")
                }
            },
            containerColor = VoxNavyCard,
        )
    }
}

@Composable
private fun HistoryCard(
    item: HistoryEntity,
    dateStr: String,
    onDelete: () -> Unit,
    onConvertToNote: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VoxNavyBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VoxNavyCard),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoxTextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Row {
                    IconButton(
                        onClick = onConvertToNote,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Convert to note",
                            tint = VoxCyan,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete session",
                            tint = VoxCoral,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = VoxTextMuted,
                )
                Text(
                    text = " · ${item.durationSeconds}s",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = VoxCyan,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.preview.ifBlank { "Voice dialogue logged." },
                fontSize = 13.sp,
                color = VoxTextSecondary,
                lineHeight = 18.sp,
            )
        }
    }
}
