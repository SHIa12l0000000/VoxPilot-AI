package com.androidengineers.agent_quickstart_android.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidengineers.agent_quickstart_android.model.LanguagePreference
import com.androidengineers.agent_quickstart_android.ui.theme.VoxCoral
import com.androidengineers.agent_quickstart_android.ui.theme.VoxCyan
import com.androidengineers.agent_quickstart_android.ui.theme.VoxIndigo
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyBorder
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyCard
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyDark
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavySurface
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextMuted
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextPrimary
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextSecondary

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults

@Composable
fun SettingsScreen(
    currentLanguage: LanguagePreference,
    onLanguageSelected: (LanguagePreference) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onClearHistory: () -> Unit,
    serverUrl: String = "",
    onServerUrlSaved: (String) -> Unit = {},
    connectionTestResult: String? = null,
    isTestingConnection: Boolean = false,
    onTestConnection: (String) -> Unit = {},
    geminiApiKey: String = "",
    onGeminiApiKeySaved: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoxNavyDark)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
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
                text = "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = VoxTextPrimary,
            )
        }
        Text(
            text = "Voice copilot preferences & local data controls",
            fontSize = 12.sp,
            color = VoxTextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 20.dp),
        )

        // Section: Server URL
        SettingsSectionHeader(icon = Icons.Default.Wifi, title = "Backend Server URL")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .border(1.dp, VoxNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoxNavyCard),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VoxPilot runs out-of-the-box on every device using on-device autonomous voice intelligence. (Optional) Enter an Agora server URL to connect with cloud streaming.",
                    fontSize = 12.sp,
                    color = VoxTextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                var urlDraft by remember(serverUrl) { mutableStateOf(serverUrl) }
                var saved by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = urlDraft,
                    onValueChange = { urlDraft = it; saved = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Server URL (Optional)", fontSize = 12.sp) },
                    placeholder = { Text("Optional (e.g. https://your-domain.com)", fontSize = 12.sp, color = VoxTextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onServerUrlSaved(urlDraft)
                        saved = true
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VoxCyan,
                        unfocusedBorderColor = VoxNavyBorder,
                        focusedLabelColor = VoxCyan,
                        unfocusedLabelColor = VoxTextMuted,
                        cursorColor = VoxCyan,
                        focusedTextColor = VoxTextPrimary,
                        unfocusedTextColor = VoxTextPrimary,
                    ),
                )

                // Connection Test Status Badge
                if (connectionTestResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VoxNavySurface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = connectionTestResult,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = VoxTextPrimary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = { onTestConnection(urlDraft) },
                        enabled = !isTestingConnection,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoxIndigo.copy(alpha = 0.25f),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            if (isTestingConnection) "Testing..." else "Test Connection",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Button(
                        onClick = { onServerUrlSaved(urlDraft); saved = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoxCyan.copy(alpha = 0.15f),
                            contentColor = VoxCyan,
                        ),
                    ) {
                        Text(if (saved) "Saved ✓" else "Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Gemini API Key (Required for AI answers)
        SettingsSectionHeader(icon = Icons.Default.Key, title = "Gemini AI — API Key")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .border(1.dp, VoxCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoxNavyCard),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔑 Required for intelligent AI answers on ANY topic.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoxCyan,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Text(
                    text = "Get your FREE Gemini API key at:\naistudio.google.com/app/apikey\n\nNo credit card needed. Works instantly.",
                    fontSize = 12.sp,
                    color = VoxTextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                var keyDraft by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
                var keySaved by remember { mutableStateOf(false) }
                var keyVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = keyDraft,
                    onValueChange = { keyDraft = it; keySaved = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Gemini API Key", fontSize = 12.sp) },
                    placeholder = { Text("Paste your API key here...", fontSize = 12.sp, color = VoxTextMuted) },
                    singleLine = true,
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Icon(
                                imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (keyVisible) "Hide key" else "Show key",
                                tint = VoxTextMuted,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onGeminiApiKeySaved(keyDraft)
                        keySaved = true
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VoxCyan,
                        unfocusedBorderColor = VoxNavyBorder,
                        focusedLabelColor = VoxCyan,
                        unfocusedLabelColor = VoxTextMuted,
                        cursorColor = VoxCyan,
                        focusedTextColor = VoxTextPrimary,
                        unfocusedTextColor = VoxTextPrimary,
                    ),
                )

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onGeminiApiKeySaved(keyDraft); keySaved = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (keySaved) VoxCyan.copy(alpha = 0.2f) else VoxIndigo.copy(alpha = 0.3f),
                        contentColor = VoxCyan,
                    ),
                ) {
                    Text(
                        if (keySaved) "✓ API Key Saved — VoxPilot can now answer anything!" else "Save API Key & Enable Gemini AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Section: Language Preference

        SettingsSectionHeader(icon = Icons.Default.Language, title = "Language")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .border(1.dp, VoxNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoxNavyCard),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                LanguagePreference.values().forEach { pref ->
                    val isSelected = currentLanguage == pref
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) VoxNavySurface else Color.Transparent)
                            .clickable { onLanguageSelected(pref) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = pref.title,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) VoxCyan else VoxTextPrimary,
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(VoxCyan),
                            )
                        }
                    }
                }
            }
        }

        // Section: Voice Preference
        SettingsSectionHeader(icon = Icons.Default.RecordVoiceOver, title = "Voice Style")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .border(1.dp, VoxNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoxNavyCard),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Natural Conversational Voice",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoxTextPrimary,
                )
                Text(
                    text = "High-fidelity, ultra-low-latency multilingual speech engine with natural intonation in English, Hindi & Hinglish.",
                    fontSize = 12.sp,
                    color = VoxTextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        // Section: Appearance
        SettingsSectionHeader(icon = Icons.Default.Palette, title = "Appearance")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .border(1.dp, VoxNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoxNavyCard),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Dark Navy Theme",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoxTextPrimary,
                    )
                    Text(
                        text = "Optimized for high contrast and focus",
                        fontSize = 12.sp,
                        color = VoxTextSecondary,
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = VoxIndigo,
                    ),
                )
            }
        }

        // Section: Data & Storage
        SettingsSectionHeader(icon = Icons.Default.DeleteSweep, title = "Data Management")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .border(1.dp, VoxNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoxNavyCard),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Clear Local History",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoxTextPrimary,
                )
                Text(
                    text = "Permanently removes all saved conversation logs from this device.",
                    fontSize = 12.sp,
                    color = VoxTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                Button(
                    onClick = onClearHistory,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoxCoral.copy(alpha = 0.2f),
                        contentColor = VoxCoral,
                    ),
                ) {
                    Text("Delete All History", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: About VoxPilot AI
        SettingsSectionHeader(icon = Icons.Default.Info, title = "About VoxPilot AI")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
                .border(1.dp, VoxNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoxNavyCard),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VoxPilot AI v1.1.0",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoxTextPrimary,
                )
                Text(
                    text = "Tagline: Talk. Interrupt. Learn. Act.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = VoxCyan,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                Text(
                    text = "Powered by Agora Conversational AI (Real-Time Communication RTC & Real-Time Messaging RTM) with Deepgram multilingual speech recognition and MiniMax low-latency synthesis.",
                    fontSize = 12.sp,
                    color = VoxTextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VoxCyan,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = VoxTextMuted,
            letterSpacing = 0.8.sp,
        )
    }
}
