package com.androidengineers.agent_quickstart_android.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidengineers.agent_quickstart_android.model.AgentVisualState
import com.androidengineers.agent_quickstart_android.model.ConversationUiState
import com.androidengineers.agent_quickstart_android.model.VoxPilotMode
import com.androidengineers.agent_quickstart_android.ui.components.LiveTranscriptView
import com.androidengineers.agent_quickstart_android.ui.components.VoiceOrb
import com.androidengineers.agent_quickstart_android.ui.theme.VoxCoral
import com.androidengineers.agent_quickstart_android.ui.theme.VoxCyan
import com.androidengineers.agent_quickstart_android.ui.theme.VoxEmerald
import com.androidengineers.agent_quickstart_android.ui.theme.VoxIndigo
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyBorder
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyCard
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyDark
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavySurface
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextMuted
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextPrimary
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextSecondary

@Composable
fun HomeScreen(
    uiState: ConversationUiState,
    onStartRequested: () -> Unit,
    onEndConversation: () -> Unit,
    onToggleMicrophone: () -> Unit,
    onModeSelected: (VoxPilotMode) -> Unit,
    onSaveNotes: () -> Unit,
    onDismissMessages: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoxNavyDark)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // --- TOP BAR: Header & Mode Selector ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(VoxCyan),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VoxPilot AI",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = VoxTextPrimary,
                )
            }
            Text(
                text = "Your real-time voice copilot",
                fontSize = 12.sp,
                color = VoxTextMuted,
                modifier = Modifier.padding(top = 2.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector row
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(VoxNavyCard)
                    .border(1.dp, VoxNavyBorder, RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                VoxPilotMode.values().forEach { mode ->
                    val isSelected = uiState.selectedMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) VoxIndigo else Color.Transparent)
                            .clickable(enabled = !uiState.inConversation) { onModeSelected(mode) }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = mode.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else VoxTextSecondary,
                        )
                    }
                }
            }
        }

        // --- CENTER: Voice Orb & State Label ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            VoiceOrb(
                state = uiState.agentVisualState,
                size = 170.dp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Agent State Label Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(VoxNavyCard)
                    .border(1.dp, VoxNavyBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = uiState.agentStateLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (uiState.agentVisualState) {
                        AgentVisualState.LISTENING -> VoxEmerald
                        AgentVisualState.SPEAKING -> VoxCyan
                        AgentVisualState.DISCONNECTED -> VoxCoral
                        else -> VoxTextPrimary
                    },
                )
            }

            // Note Save Toast indicator
            AnimatedVisibility(
                visible = uiState.noteSaveStatus != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                uiState.noteSaveStatus?.let { status ->
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VoxNavySurface)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VoxEmerald,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status,
                            fontSize = 12.sp,
                            color = VoxTextPrimary,
                        )
                    }
                }
            }

            // Error / Warning banner
            if (uiState.errorMessage != null || uiState.warningMessage != null) {
                val isError = uiState.errorMessage != null
                val message = uiState.errorMessage ?: uiState.warningMessage.orEmpty()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isError) VoxCoral.copy(alpha = 0.15f) else Color(0x33F59E0B))
                        .border(1.dp, if (isError) VoxCoral else Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isError) VoxCoral else Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            fontSize = 12.sp,
                            color = VoxTextPrimary,
                            lineHeight = 16.sp,
                        )
                    }
                    IconButton(
                        onClick = onDismissMessages,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = VoxTextMuted,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }

        // --- LOWER: Live Transcript & Primary Action Button ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LiveTranscriptView(
                history = uiState.transcriptHistory,
                liveTurn = uiState.liveTranscript,
                onSaveNotesClick = if (uiState.inConversation) onSaveNotes else null,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (uiState.inConversation) {
                    // Mute / Unmute Button
                    IconButton(
                        onClick = onToggleMicrophone,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (uiState.micEnabled) VoxNavyCard else VoxCoral.copy(alpha = 0.2f))
                            .border(1.dp, if (uiState.micEnabled) VoxNavyBorder else VoxCoral, CircleShape),
                    ) {
                        Icon(
                            imageVector = if (uiState.micEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = if (uiState.micEnabled) "Mute" else "Unmute",
                            tint = if (uiState.micEnabled) Color.White else VoxCoral,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // End Conversation Button
                    Button(
                        onClick = onEndConversation,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoxCoral,
                            contentColor = Color.White,
                        ),
                    ) {
                        if (uiState.isStopping) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ending…", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Conversation", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Start Conversation Button (Prominent Glowing Action)
                    Button(
                        onClick = onStartRequested,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoxIndigo,
                            contentColor = Color.White,
                        ),
                    ) {
                        if (uiState.isStarting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting to VoxPilot…", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Conversation", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
