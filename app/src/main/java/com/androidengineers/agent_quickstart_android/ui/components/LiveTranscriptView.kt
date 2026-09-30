package com.androidengineers.agent_quickstart_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidengineers.agent_quickstart_android.model.TranscriptSpeaker
import com.androidengineers.agent_quickstart_android.model.TranscriptTurn
import com.androidengineers.agent_quickstart_android.ui.theme.VoxCyan
import com.androidengineers.agent_quickstart_android.ui.theme.VoxIndigoLight
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyBorder
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyCard
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavySurface
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextMuted
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextPrimary
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextSecondary

@Composable
fun LiveTranscriptView(
    history: List<TranscriptTurn>,
    liveTurn: TranscriptTurn?,
    onSaveNotesClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Show only the most recent user turn and assistant turn to keep text from dominating
    val recentTurns = (history + listOfNotNull(liveTurn)).takeLast(2)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VoxNavyCard)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VoxCyan),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIVE TRANSCRIPT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = VoxTextMuted,
                )
            }

            if (onSaveNotesClick != null && recentTurns.isNotEmpty()) {
                IconButton(
                    onClick = onSaveNotesClick,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Save as note",
                        tint = VoxCyan,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (recentTurns.isEmpty()) {
            Text(
                text = "Speak naturally. VoxPilot listens, teaches, and interrupts smoothly.",
                fontSize = 14.sp,
                color = VoxTextMuted,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            recentTurns.forEach { turn ->
                val isUser = turn.speaker == TranscriptSpeaker.USER
                val speakerLabel = if (isUser) "YOU" else "VOX"
                val labelColor = if (isUser) VoxCyan else VoxIndigoLight
                val bubbleBg = if (isUser) VoxNavySurface else VoxNavyBorder.copy(alpha = 0.5f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bubbleBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = speakerLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelColor,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = turn.text.ifBlank { "…" },
                        fontSize = 14.sp,
                        color = VoxTextPrimary,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }
}
