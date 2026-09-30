package com.androidengineers.agent_quickstart_android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.androidengineers.agent_quickstart_android.data.history.HistoryEntity
import com.androidengineers.agent_quickstart_android.data.notes.NoteEntity
import com.androidengineers.agent_quickstart_android.model.ConversationUiState
import com.androidengineers.agent_quickstart_android.model.LanguagePreference
import com.androidengineers.agent_quickstart_android.model.NavigationTab
import com.androidengineers.agent_quickstart_android.model.VoxPilotMode
import com.androidengineers.agent_quickstart_android.ui.components.VoxPilotBottomBar
import com.androidengineers.agent_quickstart_android.ui.history.HistoryScreen
import com.androidengineers.agent_quickstart_android.ui.home.HomeScreen
import com.androidengineers.agent_quickstart_android.ui.notes.NotesScreen
import com.androidengineers.agent_quickstart_android.ui.settings.SettingsScreen
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyDark

@Composable
fun ConversationScreen(
    uiState: ConversationUiState,
    notes: List<NoteEntity> = emptyList(),
    history: List<HistoryEntity> = emptyList(),
    onStartRequested: () -> Unit,
    onEndConversation: () -> Unit,
    onToggleMicrophone: () -> Unit,
    onToggleTheme: () -> Unit,
    onDismissMessages: () -> Unit,
    onModeSelected: (VoxPilotMode) -> Unit = {},
    onLanguageSelected: (LanguagePreference) -> Unit = {},
    onTabSelected: (NavigationTab) -> Unit = {},
    onSaveNotes: () -> Unit = {},
    onDeleteNote: (Long) -> Unit = {},
    onDeleteHistory: (Long) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onConvertToNote: (HistoryEntity) -> Unit = {},
    onTextChanged: (String) -> Unit = {},
    onSendText: (Boolean, Boolean) -> Unit = { _, _ -> },
    serverUrl: String = "",
    onServerUrlSaved: (String) -> Unit = {},
    connectionTestResult: String? = null,
    isTestingConnection: Boolean = false,
    onTestConnection: (String) -> Unit = {},
    geminiApiKey: String = "",
    onGeminiApiKeySaved: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(VoxNavyDark)
            .statusBarsPadding(),
        bottomBar = {
            VoxPilotBottomBar(
                currentTab = uiState.currentTab,
                onTabSelected = onTabSelected,
            )
        },
        containerColor = VoxNavyDark,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState.currentTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onStartRequested = onStartRequested,
                        onEndConversation = onEndConversation,
                        onToggleMicrophone = onToggleMicrophone,
                        onModeSelected = onModeSelected,
                        onSaveNotes = onSaveNotes,
                        onDismissMessages = onDismissMessages,
                    )
                }

                NavigationTab.NOTES -> {
                    NotesScreen(
                        notes = notes,
                        onDeleteNote = onDeleteNote,
                    )
                }

                NavigationTab.HISTORY -> {
                    HistoryScreen(
                        historyList = history,
                        onDeleteHistory = onDeleteHistory,
                        onConvertToNote = onConvertToNote,
                    )
                }

                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        currentLanguage = uiState.selectedLanguage,
                        onLanguageSelected = onLanguageSelected,
                        isDarkTheme = uiState.isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onClearHistory = onClearHistory,
                        serverUrl = serverUrl,
                        onServerUrlSaved = onServerUrlSaved,
                        connectionTestResult = connectionTestResult,
                        isTestingConnection = isTestingConnection,
                        onTestConnection = onTestConnection,
                        geminiApiKey = geminiApiKey,
                        onGeminiApiKeySaved = onGeminiApiKeySaved,
                    )
                }
            }
        }
    }
}
