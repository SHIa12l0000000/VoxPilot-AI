package com.androidengineers.agent_quickstart_android.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.androidengineers.agent_quickstart_android.config.QuickstartConfig
import com.androidengineers.agent_quickstart_android.config.ServerUrlPrefs
import com.androidengineers.agent_quickstart_android.data.ConversationAgoraApi
import com.androidengineers.agent_quickstart_android.data.ConversationRepository
import com.androidengineers.agent_quickstart_android.model.AgentVisualState
import com.androidengineers.agent_quickstart_android.model.ConversationUiState
import com.androidengineers.agent_quickstart_android.rtc.AgoraConversationSessionManager
import com.androidengineers.agent_quickstart_android.voice.VoxPilotVoiceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

import com.androidengineers.agent_quickstart_android.data.history.HistoryEntity
import com.androidengineers.agent_quickstart_android.data.history.HistoryRepository
import com.androidengineers.agent_quickstart_android.data.notes.NoteEntity
import com.androidengineers.agent_quickstart_android.data.notes.NotesRepository
import com.androidengineers.agent_quickstart_android.model.LanguagePreference
import com.androidengineers.agent_quickstart_android.model.NavigationTab
import com.androidengineers.agent_quickstart_android.model.TranscriptSpeaker
import com.androidengineers.agent_quickstart_android.model.TranscriptTurn
import com.androidengineers.agent_quickstart_android.model.VoxPilotMode

class ConversationViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val sessionManager = AgoraConversationSessionManager(application)
    private val notesRepository = NotesRepository(application)
    private val historyRepository = HistoryRepository(application)

    // Dynamic repository — rebuilt each conversation with the current server URL
    private var repository = ConversationRepository(
        ConversationAgoraApi(ServerUrlPrefs.effectiveUrl(application))
    )

    val allNotes = notesRepository.allNotes
    val allHistory = historyRepository.allHistory

    private val _uiState = MutableStateFlow(ConversationUiStateMapper.freshUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private val _serverUrl = MutableStateFlow(ServerUrlPrefs.effectiveUrl(getApplication()))
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(
        getApplication<android.app.Application>()
            .getSharedPreferences("voxpilot_prefs", android.content.Context.MODE_PRIVATE)
            .getString("gemini_api_key", "") ?: ""
    )
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    fun setGeminiApiKey(key: String) {
        val trimmed = key.trim()
        _geminiApiKey.value = trimmed
        getApplication<android.app.Application>()
            .getSharedPreferences("voxpilot_prefs", android.content.Context.MODE_PRIVATE)
            .edit().putString("gemini_api_key", trimmed).apply()
        if (isAutonomousActive) {
            autonomousEngine.setApiKey(trimmed)
        }
    }

    private var activeAgentId: String? = null
    private var themeInitialized: Boolean = false
    private var sessionStartTimeMs: Long = 0L
    private var lastAutoSavedNoteTurnId: Long = -1L
    private var isAutonomousActive = false

    private val autonomousEngine: VoxPilotVoiceEngine by lazy {
        VoxPilotVoiceEngine(
            context = getApplication(),
            listener = object : VoxPilotVoiceEngine.EngineListener {
                override fun onVisualStateChanged(state: AgentVisualState, label: String) {
                    _uiState.update {
                        it.copy(
                            agentVisualState = state,
                            agentStateLabel = label,
                        )
                    }
                }

                override fun onLiveTurnUpdated(turn: TranscriptTurn?) {
                    _uiState.update { it.copy(liveTranscript = turn) }
                }

                override fun onTurnCommitted(turn: TranscriptTurn) {
                    _uiState.update {
                        it.copy(
                            transcriptHistory = it.transcriptHistory + turn,
                            liveTranscript = null,
                        )
                    }
                }

                override fun onVoiceNoteTriggerDetected() {
                    saveNoteFromCurrentConversation()
                }

                override fun onError(message: String) {
                    _uiState.update { it.copy(warningMessage = message) }
                }
            }
        )
    }

    private val _connectionTestResult = MutableStateFlow<String?>(null)
    val connectionTestResult: StateFlow<String?> = _connectionTestResult.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    fun setServerUrl(url: String) {
        val trimmed = url.trim()
        ServerUrlPrefs.setServerUrl(getApplication(), trimmed)
        val effective = trimmed.ifBlank { ServerUrlPrefs.effectiveUrl(getApplication()) }
        _serverUrl.value = effective
        QuickstartConfig.runtimeBaseUrl = effective
        // Rebuild repository so next conversation uses the new URL
        repository = ConversationRepository(ConversationAgoraApi(effective))
        _connectionTestResult.value = null
    }

    fun testServerConnection(url: String) {
        val targetUrl = url.trim().ifBlank { _serverUrl.value }
        viewModelScope.launch {
            _isTestingConnection.value = true
            _connectionTestResult.value = "Connecting to $targetUrl..."
            val start = SystemClock.elapsedRealtime()
            val result = runCatching {
                val tempApi = ConversationAgoraApi(targetUrl)
                tempApi.checkHealth()
            }
            val elapsed = SystemClock.elapsedRealtime() - start
            _isTestingConnection.value = false
            result.onSuccess { health ->
                _connectionTestResult.value = "🟢 Connected! (status: ${health.status}, ${elapsed}ms)"
            }.onFailure { error ->
                val errorMsg = error.message.orEmpty()
                _connectionTestResult.value = when {
                    errorMsg.contains("failed to connect", ignoreCase = true) ->
                        "🔴 Unreachable: Check if server is running on host IP"
                    errorMsg.contains("timeout", ignoreCase = true) ->
                        "🔴 Timeout: Server did not respond in time"
                    else -> "🔴 Failed: ${errorMsg.take(80)}"
                }
            }
        }
    }

    init {
        val initialUrl = ServerUrlPrefs.effectiveUrl(application)
        QuickstartConfig.runtimeBaseUrl = initialUrl
        viewModelScope.launch {
            sessionManager.snapshot.collectLatest { snapshot ->
                _uiState.update { current ->
                    ConversationUiStateMapper.mergeSession(current, snapshot)
                }

                // Check for voice command triggers like "save this as notes"
                checkForVoiceNoteTriggers(snapshot.transcriptTurns)
            }
        }
    }

    private fun checkForVoiceNoteTriggers(turns: List<TranscriptTurn>) {
        val lastUserTurn = turns.lastOrNull { it.speaker == TranscriptSpeaker.USER } ?: return
        if (lastUserTurn.turnId == lastAutoSavedNoteTurnId) return

        val text = lastUserTurn.text.lowercase().trim()
        val isSaveTrigger = text.contains("save this") ||
            text.contains("make note") ||
            text.contains("make notes") ||
            text.contains("summarize what we discussed") ||
            text.contains("notes banao") ||
            text.contains("save notes") ||
            text.contains("save as note")

        if (isSaveTrigger && turns.size >= 2) {
            lastAutoSavedNoteTurnId = lastUserTurn.turnId
            saveNoteFromCurrentConversation()
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun saveNoteFromCurrentConversation() {
        val turns = sessionManager.snapshot.value.transcriptTurns.ifEmpty {
            _uiState.value.transcriptHistory
        }
        if (turns.isEmpty()) {
            _uiState.update { it.copy(noteSaveStatus = "No conversation transcript to save.") }
            return
        }

        viewModelScope.launch {
            val note = notesRepository.createNoteFromTranscripts(turns)
            if (note != null) {
                _uiState.update {
                    it.copy(noteSaveStatus = "Note saved: \"${note.title}\"")
                }
            } else {
                _uiState.update {
                    it.copy(noteSaveStatus = "Could not generate note from empty dialogue.")
                }
            }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            notesRepository.deleteNote(id)
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    fun convertHistoryToNote(history: HistoryEntity) {
        viewModelScope.launch {
            val note = historyRepository.convertToNote(history)
            if (note != null) {
                _uiState.update {
                    it.copy(
                        noteSaveStatus = "Converted to note: \"${note.title}\"",
                        currentTab = NavigationTab.NOTES,
                    )
                }
            }
        }
    }

    fun clearNoteSaveStatus() {
        _uiState.update { it.copy(noteSaveStatus = null) }
    }

    fun updateMicrophonePermission(granted: Boolean) {
        _uiState.update { it.copy(microphonePermissionGranted = granted) }
    }

    fun initializeTheme(systemDarkTheme: Boolean) {
        if (themeInitialized) {
            return
        }
        themeInitialized = true
        _uiState.update { it.copy(isDarkTheme = systemDarkTheme) }
    }

    fun toggleTheme() {
        themeInitialized = true
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun setMode(mode: VoxPilotMode) {
        _uiState.update { it.copy(selectedMode = mode) }
        if (isAutonomousActive) {
            autonomousEngine.setMode(mode)
        }
    }

    fun setLanguagePreference(pref: LanguagePreference) {
        _uiState.update { it.copy(selectedLanguage = pref) }
        if (isAutonomousActive) {
            autonomousEngine.setLanguage(pref)
        }
    }

    fun startConversation() {
        val currentState = _uiState.value
        if (currentState.isStarting || currentState.isStopping) {
            return
        }
        if (!currentState.microphonePermissionGranted) {
            _uiState.update {
                it.copy(
                    errorMessage = "Microphone access is required to speak with VoxPilot.",
                    warningMessage = null,
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isStarting = true,
                    errorMessage = null,
                    warningMessage = null,
                )
            }
            repository = ConversationRepository(ConversationAgoraApi(_serverUrl.value))

            // Attempt Agora backend connection with quick 2.5s timeout
            val agoraSuccess = withTimeoutOrNull(2500L) {
                runCatching {
                    val healthStartedAt = SystemClock.elapsedRealtime()
                    val health = repository.checkHealth()
                    _uiState.update {
                        it.copy(
                            backendLatencyMs = SystemClock.elapsedRealtime() - healthStartedAt,
                            lastServerResponse = "${health.status} (${health.version})",
                        )
                    }
                    val bootstrap = repository.requestSessionBootstrap()
                    sessionManager.connect(bootstrap) { channel, rtcUid, rtmUserId ->
                        repository.renewTokens(
                            channel = channel,
                            rtcUid = rtcUid,
                            rtmUserId = rtmUserId,
                        )
                    }

                    sessionStartTimeMs = SystemClock.elapsedRealtime()
                    val requesterRtcUid = sessionManager.snapshot.value.localRtcUid
                        .takeIf { it > 0 }
                        ?.toString()
                        ?: bootstrap.uid
                    val inviteAttempt = runCatching {
                        repository.inviteAgent(
                            channelName = bootstrap.channel,
                            requesterRtcUid = requesterRtcUid,
                            mode = currentState.selectedMode.serverKey,
                            language = currentState.selectedLanguage.serverKey,
                        )
                    }
                    val inviteResult = inviteAttempt.getOrNull()
                    activeAgentId = inviteResult?.agentId
                    sessionManager.setActiveAgentId(activeAgentId)

                    val warning = inviteAttempt.exceptionOrNull()?.message?.let { message ->
                        "Joined channel, but agent start reported: $message"
                    }

                    _uiState.update { current ->
                        ConversationUiStateMapper.mergeSession(
                            current.copy(
                                isStarting = false,
                                inConversation = true,
                                canSendText = activeAgentId != null,
                                warningMessage = warning,
                            ),
                            sessionManager.snapshot.value,
                        )
                    }
                    true
                }.getOrNull()
            } == true

            if (agoraSuccess) {
                isAutonomousActive = false
            } else {
                // Seamlessly start Autonomous Engine so it works on ANY device without requiring any IP address!
                isAutonomousActive = true
                sessionStartTimeMs = SystemClock.elapsedRealtime()
                _uiState.update {
                    it.copy(
                        isStarting = false,
                        inConversation = true,
                        canSendText = true,
                        agentVisualState = AgentVisualState.LISTENING,
                        agentStateLabel = "Listening (VoxPilot Autonomous Engine)...",
                        lastServerResponse = "Autonomous Mode Active",
                        warningMessage = null,
                        errorMessage = null,
                    )
                }
                autonomousEngine.start(currentState.selectedMode, currentState.selectedLanguage)
            }
        }
    }

    fun endConversation() {
        val currentState = _uiState.value
        if (currentState.isStopping || currentState.isStarting) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isStopping = true) }

            // Save session to history before disconnecting
            val turnsToSave = if (isAutonomousActive) {
                _uiState.value.transcriptHistory
            } else {
                sessionManager.snapshot.value.transcriptTurns
            }
            val durationSeconds = if (sessionStartTimeMs > 0L) {
                ((SystemClock.elapsedRealtime() - sessionStartTimeMs) / 1000L).coerceAtLeast(1L)
            } else 0L

            if (turnsToSave.isNotEmpty()) {
                runCatching {
                    historyRepository.saveSession(turnsToSave, durationSeconds)
                }
            }
            sessionStartTimeMs = 0L

            if (isAutonomousActive) {
                autonomousEngine.stop()
                isAutonomousActive = false
            } else {
                val warning = activeAgentId?.let { agentId ->
                    val channelName = sessionManager.snapshot.value.channelName
                    runCatching {
                        if (channelName != null) {
                            repository.stopConversation(agentId, channelName)
                        }
                    }.exceptionOrNull()?.message?.let { message ->
                        "The local session ended, but the server leave request failed: $message"
                    }
                }
                activeAgentId = null
                sessionManager.setActiveAgentId(null)
                sessionManager.disconnect(resetSnapshot = true)
            }

            _uiState.value = ConversationUiStateMapper.freshUiState(
                permissionGranted = _uiState.value.microphonePermissionGranted,
                isDarkTheme = _uiState.value.isDarkTheme,
                mode = _uiState.value.selectedMode,
                language = _uiState.value.selectedLanguage,
                currentTab = _uiState.value.currentTab,
            )
        }
    }

    fun toggleMicrophone() {
        val newEnabled = !_uiState.value.micRequestedEnabled
        _uiState.update { it.copy(micRequestedEnabled = newEnabled, micEnabled = newEnabled) }
        if (isAutonomousActive) {
            autonomousEngine.setMuted(!newEnabled)
        } else {
            sessionManager.setMicrophoneEnabled(newEnabled)
        }
    }

    fun updateTextDraft(text: String) {
        _uiState.update { it.copy(textDraft = text.take(2000), textActionStatus = null) }
    }

    fun sendText(speak: Boolean, append: Boolean) {
        val state = _uiState.value
        val text = state.textDraft.trim()
        if (text.isEmpty() || state.isSendingText || state.isStopping || !state.inConversation) return

        if (isAutonomousActive) {
            _uiState.update { it.copy(textDraft = "", textActionStatus = "Processed") }
            autonomousEngine.handleTextInstruction(text)
            return
        }

        val agentId = activeAgentId ?: return
        val channel = sessionManager.snapshot.value.channelName ?: return
        _uiState.update { it.copy(isSendingText = true, textActionStatus = null) }
        viewModelScope.launch {
            val result = runCatching { repository.sendText(agentId, channel, text, speak, append) }
            if (activeAgentId != agentId) return@launch
            _uiState.update {
                it.copy(
                    isSendingText = false,
                    textDraft = if (result.isSuccess && it.textDraft == state.textDraft) "" else it.textDraft,
                    textActionStatus = if (result.isSuccess) {
                        if (speak) "Speech request accepted" else "Instruction accepted"
                    } else null,
                    errorMessage = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun clearTransientMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                warningMessage = null,
            )
        }
    }

    override fun onCleared() {
        if (isAutonomousActive) {
            autonomousEngine.release()
        }
        sessionManager.release()
        super.onCleared()
    }
}

