package com.androidengineers.agent_quickstart_android.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidengineers.agent_quickstart_android.data.notes.NoteEntity
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
fun NotesScreen(
    notes: List<NoteEntity>,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())

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
                text = "Smart Notes",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = VoxTextPrimary,
            )
        }
        Text(
            text = "AI-generated summaries and key points saved locally",
            fontSize = 12.sp,
            color = VoxTextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
        )

        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = VoxNavyBorder,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No notes yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoxTextSecondary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Say \"Save this as notes\" during a conversation or tap the bookmark icon on the live transcript.",
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
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        dateStr = dateFormat.format(Date(note.createdAt)),
                        onDelete = { onDeleteNote(note.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    dateStr: String,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VoxNavyBorder, RoundedCornerShape(16.dp)),
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
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoxTextPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete note",
                        tint = VoxCoral,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = dateStr,
                fontSize = 11.sp,
                color = VoxTextMuted,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = note.summary,
                fontSize = 13.sp,
                color = VoxTextSecondary,
                lineHeight = 18.sp,
            )

            if (note.keyPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VoxNavySurface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "KEY TAKEAWAYS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoxCyan,
                        letterSpacing = 0.8.sp,
                    )
                    note.keyPoints.forEach { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                text = "• ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VoxCyan,
                            )
                            Text(
                                text = point,
                                fontSize = 12.sp,
                                color = VoxTextPrimary,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
