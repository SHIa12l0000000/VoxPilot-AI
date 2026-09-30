package com.androidengineers.agent_quickstart_android.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.androidengineers.agent_quickstart_android.model.AgentVisualState
import com.androidengineers.agent_quickstart_android.model.LanguagePreference
import com.androidengineers.agent_quickstart_android.model.TranscriptSpeaker
import com.androidengineers.agent_quickstart_android.model.TranscriptTurn
import com.androidengineers.agent_quickstart_android.model.TranscriptTurnStatus
import com.androidengineers.agent_quickstart_android.model.VoxPilotMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Autonomous On-Device Voice Copilot Engine — Powered by Gemini AI.
 *
 * Uses HttpURLConnection (no external library needed) to call Gemini 2.0 Flash.
 * Falls back to smart built-in responses when offline or key not set.
 */
class VoxPilotVoiceEngine(
    private val context: Context,
    private val listener: EngineListener,
) : TextToSpeech.OnInitListener {

    interface EngineListener {
        fun onVisualStateChanged(state: AgentVisualState, label: String)
        fun onLiveTurnUpdated(turn: TranscriptTurn?)
        fun onTurnCommitted(turn: TranscriptTurn)
        fun onVoiceNoteTriggerDetected()
        fun onError(message: String)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val bgExecutor = Executors.newSingleThreadExecutor()

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var isRunning = false
    private var isMuted = false
    private var isSpeaking = false
    private var isThinking = false

    private var currentMode: VoxPilotMode = VoxPilotMode.LEARN
    private var currentLanguage: LanguagePreference = LanguagePreference.AUTO
    private var turnCounter = 0L
    private var lastSpokenText: String = ""


    // ─── Gemini API Key (pre-loaded, user can update via Settings) ────────────
    private val DEFAULT_API_KEY = "YOUR_GEMINI_API_KEY_HERE" // Get free key from aistudio.google.com

    private val prefs = context.getSharedPreferences("voxpilot_prefs", Context.MODE_PRIVATE)

    private var geminiApiKey: String
        get() {
            val stored = prefs.getString("gemini_api_key", "") ?: ""
            return if (stored.isBlank() || stored.startsWith("AIzaSy")) DEFAULT_API_KEY else stored
        }
        set(value) = prefs.edit().putString("gemini_api_key", value).apply()


    // Multi-turn conversation history for contextual memory (last 20 messages)
    private val conversationHistory = mutableListOf<Pair<String, String>>() // role -> text

    // System prompts per mode — speech-optimized, language-matched, complete explanations
    private val systemPrompts = mapOf(
        VoxPilotMode.LEARN to """You are VoxPilot AI, an elite interactive voice copilot and tutor.
CRITICAL FORMATTING RULES FOR SPOKEN VOICE:
1. LANGUAGE MATCH: ALWAYS respond in the EXACT language the user used. If the user asks in English, reply in 100% natural, fluent English. ONLY speak Hindi or Hinglish if the user explicitly spoke in Hindi or Hinglish!
2. NO MARKDOWN: NEVER use markdown headers (###), asterisks (**bold**), bullet points (* or -), or numbered lists (1., 2.). Write in natural, fluid spoken paragraphs that sound conversational when read aloud by Text-To-Speech.
3. STRUCTURE & COMPLETENESS: Deliver a comprehensive, high-quality, 3-part spoken explanation:
   - Part 1: Clear, formal definition and origin.
   - Part 2: Key features, real-world applications, and why it matters.
   - Part 3: An intuitive real-world analogy, followed by a natural question asking what they would like to explore next.
4. FINISH COMPLETELY: Always finish your thoughts with a proper concluding sentence. Never cut off mid-sentence. Keep total length around 120 to 160 words — optimal for voice comprehension.""",
        VoxPilotMode.ASK to """You are VoxPilot AI, an articulate and brilliant voice copilot.
CRITICAL RULES FOR SPOKEN VOICE:
1. LANGUAGE MATCH: Respond in the EXACT language the user spoke. If English -> reply in pure fluent English. If Hindi -> reply in natural Hindi/Hinglish.
2. NO MARKDOWN: Do NOT use markdown symbols like **, ###, *, or bullet lists. Speak in smooth, natural sentences designed for Text-to-Speech audio.
3. COMPLETE & ACCURATE: Provide a thorough, informative, and complete answer with practical context. Always conclude with a natural ending sentence.""",
        VoxPilotMode.TRANSLATE to """You are VoxPilot AI, a real-time speech translator.
When user speaks English, translate to natural Hindi and say: "Hindi mein: [spoken translation]"
When user speaks Hindi or Hinglish, translate to English and say: "In English: [spoken translation]"
Do not use markdown formatting. Spoken translation only."""
    )



    init {
        mainHandler.post {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    mainHandler.post {
                        isSpeaking = true
                        listener.onVisualStateChanged(AgentVisualState.SPEAKING, "VoxPilot Speaking...")
                    }
                }

                override fun onDone(utteranceId: String?) {
                    mainHandler.post {
                        isSpeaking = false
                        if (isRunning && !isMuted) {
                            mainHandler.postDelayed({
                                if (isRunning && !isMuted && !isSpeaking) {
                                    startListeningInternal()
                                }
                            }, 450L)
                        } else {
                            listener.onVisualStateChanged(AgentVisualState.IDLE, "Ready when you are")
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        isSpeaking = false
                        if (isRunning && !isMuted) {
                            mainHandler.postDelayed({
                                if (isRunning && !isMuted && !isSpeaking) {
                                    startListeningInternal()
                                }
                            }, 450L)
                        }
                    }
                }

            })
        }
    }

    fun start(mode: VoxPilotMode, language: LanguagePreference) {
        isRunning = true
        isMuted = false
        currentMode = mode
        currentLanguage = language
        conversationHistory.clear()

        mainHandler.post {
            initRecognizerIfNeeded()
            val welcomeText = when (mode) {
                VoxPilotMode.LEARN -> "Hello! I'm VoxPilot, your interactive AI tutor powered by Gemini. What would you like to learn today?"
                VoxPilotMode.TRANSLATE -> "VoxPilot Translator ready. Speak in English or Hindi and I will translate in real time."
                VoxPilotMode.ASK -> "VoxPilot online, powered by Gemini AI. Ask me anything — any topic, any question!"
            }
            speak(welcomeText)
        }
    }

    fun stop() {
        isRunning = false
        mainHandler.post {
            stopSpeaking()
            stopListeningInternal()
            destroyRecognizer()
            listener.onVisualStateChanged(AgentVisualState.IDLE, "Ready when you are")
            listener.onLiveTurnUpdated(null)
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        mainHandler.post {
            if (muted) {
                stopSpeaking()
                stopListeningInternal()
                listener.onVisualStateChanged(AgentVisualState.IDLE, "Microphone muted")
            } else if (isRunning) {
                startListeningInternal()
            }
        }
    }

    fun setMode(mode: VoxPilotMode) {
        currentMode = mode
        conversationHistory.clear()
    }

    fun setLanguage(language: LanguagePreference) {
        currentLanguage = language
    }

    fun setApiKey(key: String) {
        geminiApiKey = key.trim()
        conversationHistory.clear()
    }

    fun getApiKey(): String = geminiApiKey

    fun handleTextInstruction(text: String) {
        if (!isRunning) return
        mainHandler.post { handleUserSpeech(text) }
    }

    // ─── Internal helpers ──────────────────────────────────────────────────────

    private fun triggerBargeIn() {
        if (isSpeaking) {
            stopSpeaking()
            listener.onVisualStateChanged(AgentVisualState.LISTENING, "Listening to you...")
        }
    }

    private fun stopSpeaking() {
        if (isSpeaking) {
            tts?.stop()
            isSpeaking = false
        }
    }

    private fun cleanTextForSpeech(text: String): String {
        return text
            .replace(Regex("[*#_~`>-]+"), " ")
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun speak(text: String) {
        val cleanSpoken = cleanTextForSpeech(text)
        if (!isRunning || cleanSpoken.isBlank()) return
        stopListeningInternal()
        isSpeaking = true
        lastSpokenText = cleanSpoken
        listener.onVisualStateChanged(AgentVisualState.SPEAKING, "VoxPilot Speaking...")

        val turnId = ++turnCounter
        val agentTurn = TranscriptTurn(
            key = UUID.randomUUID().toString(),
            turnId = turnId,
            streamId = 1L,
            speaker = TranscriptSpeaker.AGENT,
            text = cleanSpoken,
            status = TranscriptTurnStatus.END,
            createdAtMillis = System.currentTimeMillis(),
        )
        listener.onTurnCommitted(agentTurn)
        listener.onLiveTurnUpdated(null)

        if (isTtsReady && tts != null) {
            val isHindi = isHindiText(cleanSpoken) || currentLanguage == LanguagePreference.HINDI_HINGLISH
            @Suppress("DEPRECATION")
            val locale = if (isHindi) Locale("hi", "IN") else Locale.US
            tts?.language = locale
            tts?.setSpeechRate(1.02f)
            tts?.setPitch(1.0f)
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "vox_$turnId")
            }
            tts?.speak(cleanSpoken, TextToSpeech.QUEUE_FLUSH, params, "vox_$turnId")
        } else {
            mainHandler.postDelayed({
                isSpeaking = false
                if (isRunning && !isMuted) startListeningInternal()
            }, 3000L)
        }
    }


    private fun initRecognizerIfNeeded() {
        if (speechRecognizer == null && SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        if (isRunning && !isSpeaking)
                            listener.onVisualStateChanged(AgentVisualState.LISTENING, "Listening...")
                    }

                    override fun onBeginningOfSpeech() {
                        triggerBargeIn()
                        listener.onVisualStateChanged(AgentVisualState.LISTENING, "Listening...")
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        listener.onVisualStateChanged(AgentVisualState.THINKING, "Thinking...")
                    }

                    override fun onError(error: Int) {
                        if (isRunning && !isMuted && !isSpeaking && !isThinking) {
                            mainHandler.postDelayed({
                                if (isRunning && !isMuted && !isSpeaking && !isThinking)
                                    startListeningInternal()
                            }, 500L)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val text = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotBlank()) {
                            handleUserSpeech(text)
                        } else if (isRunning && !isMuted && !isSpeaking && !isThinking) {
                            startListeningInternal()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()?.trim().orEmpty()
                        if (partial.isNotBlank()) {
                            triggerBargeIn()
                            listener.onLiveTurnUpdated(
                                TranscriptTurn(
                                    key = "live_partial",
                                    turnId = turnCounter + 1,
                                    streamId = 0L,
                                    speaker = TranscriptSpeaker.USER,
                                    text = partial,
                                    status = TranscriptTurnStatus.IN_PROGRESS,
                                    createdAtMillis = System.currentTimeMillis(),
                                )
                            )
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun startListeningInternal() {
        if (!isRunning || isMuted || isSpeaking || isThinking) return
        mainHandler.post {
            initRecognizerIfNeeded()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                val langTag = when (currentLanguage) {
                    LanguagePreference.HINDI_HINGLISH -> "hi-IN"
                    LanguagePreference.ENGLISH -> "en-US"
                    LanguagePreference.AUTO -> Locale.getDefault().toLanguageTag()
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            }
            try {
                speechRecognizer?.startListening(intent)
                listener.onVisualStateChanged(AgentVisualState.LISTENING, "Listening...")
            } catch (_: Exception) {}
        }
    }

    private fun stopListeningInternal() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
    }

    private fun destroyRecognizer() {
        try { speechRecognizer?.destroy() } catch (_: Exception) {}
        speechRecognizer = null
    }

    // ─── Speech handler ────────────────────────────────────────────────────────

    private fun handleUserSpeech(rawText: String) {
        if (isThinking || isSpeaking) return

        // Self-echo filter: if speech is an echo of what VoxPilot just said, discard!
        if (lastSpokenText.isNotBlank()) {
            val cleanInput = rawText.lowercase().replace(Regex("[^a-z0-9 ]"), "").trim()
            val cleanLast = lastSpokenText.lowercase().replace(Regex("[^a-z0-9 ]"), "").trim()
            if (cleanInput.length > 4 && (cleanLast.contains(cleanInput) || cleanInput.contains(cleanLast.take(30)))) {
                return
            }
        }

        val userTurn = TranscriptTurn(
            key = UUID.randomUUID().toString(),
            turnId = ++turnCounter,
            streamId = 0L,
            speaker = TranscriptSpeaker.USER,
            text = rawText,
            status = TranscriptTurnStatus.END,
            createdAtMillis = System.currentTimeMillis(),
        )
        listener.onTurnCommitted(userTurn)
        listener.onLiveTurnUpdated(null)

        val lower = rawText.lowercase().trim()

        // Smart Notes triggers
        if (lower.contains("save this") || lower.contains("make note") ||
            lower.contains("notes banao") || lower.contains("save note")) {
            listener.onVoiceNoteTriggerDetected()
            speak(if (isHindiText(rawText)) "Maine aapki baat notes mein save kar liya!" else "Saved this conversation as a note!")
            return
        }

        isThinking = true
        listener.onVisualStateChanged(AgentVisualState.THINKING, "Thinking...")

        // Run API call on background thread
        bgExecutor.submit {
            val response = callGeminiApi(rawText)
            mainHandler.post {
                isThinking = false
                speak(response)
            }
        }
    }

    // ─── Gemini API via HttpURLConnection (no OkHttp needed) ──────────────────

    private fun callGeminiApi(userMessage: String): String {
        val apiKey = geminiApiKey
        if (apiKey.isBlank()) return getSmartFallback(userMessage)

        conversationHistory.add("user" to userMessage)
        if (conversationHistory.size > 20) conversationHistory.removeAt(0)

        val systemPrompt = systemPrompts[currentMode] ?: systemPrompts[VoxPilotMode.ASK]!!

        // Build JSON body
        val contentsArray = JSONArray()
        for ((role, text) in conversationHistory) {
            contentsArray.put(
                JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(JSONObject().put("text", text)))
            )
        }

        val requestJson = JSONObject()
            .put("system_instruction", JSONObject()
                .put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            .put("contents", contentsArray)
            .put("generationConfig", JSONObject()
                .put("temperature", 0.7)
                .put("maxOutputTokens", 400))
            .toString()



        val modelsToTry = listOf("gemini-3.1-flash-lite", "gemini-3.5-flash")

        for (model in modelsToTry) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.doOutput = true
                conn.connectTimeout = 10_000
                conn.readTimeout = 20_000

                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(requestJson) }

                val responseCode = conn.responseCode
                val responseBody = if (responseCode in 200..299) {
                    conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                } else {
                    conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""
                }
                conn.disconnect()

                if (responseCode in 200..299) {
                    val json = JSONObject(responseBody)
                    val aiText = json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                        .trim()

                    if (aiText.isNotBlank()) {
                        conversationHistory.add("model" to aiText)
                        return aiText
                    }
                }
            } catch (_: Exception) {}
        }

        return getSmartFallback(userMessage)
    }

    // ─── Fallback responses ────────────────────────────────────────────────────

    private fun getSmartFallback(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("python") ->
                "Python is a versatile, high-level programming language known for its clean syntax, readability, and vast library ecosystem across AI and web development."
            lower.contains("java") && lower.contains("difference") ->
                "Java is a statically typed, compiled object-oriented language for high performance, while Python is dynamically typed with concise, human-readable syntax."
            lower.contains("binary search") ->
                "Binary Search is an efficient O(log n) algorithm that finds elements in a sorted list by repeatedly halving the search space."
            lower.contains("machine learning") ->
                "Machine Learning enables systems to automatically learn and improve from experience without being explicitly programmed."
            lower.contains("hindi") || lower.contains("samjhao") || lower.contains("kya hai") ->
                "Bilkul! Main har technical vishay ko asaan shabdon mein Hindi aur Hinglish mein samjha sakta hoon. Aap agla sawaal poochiye!"
            lower.matches(Regex("^(hi|hello|hey|namaste).*")) ->
                "Hello! I'm VoxPilot AI powered by Gemini. Ask me anything!"
            else ->
                "I'm VoxPilot AI. What specific concept or detail would you like to explore?"
        }
    }


    private fun getNetworkErrorFallback(input: String): String =
        if (isHindiText(input))
            "Network se connect nahi ho pa raha. Please internet connection check karein."
        else
            "I couldn't reach the AI right now. Please check your internet connection and try again."

    private fun isHindiText(text: String): Boolean {
        for (char in text) {
            if (Character.UnicodeBlock.of(char) == Character.UnicodeBlock.DEVANAGARI) return true
        }
        val lower = text.lowercase()
        return lower.contains("kya") || lower.contains("hai") || lower.contains("kaise") ||
            lower.contains("mein") || lower.contains("batao") || lower.contains("samjhao") ||
            lower.contains("karo") || lower.contains("haan") || lower.contains("nahi")
    }

    fun release() {
        stop()
        mainHandler.post {
            tts?.shutdown()
            tts = null
        }
        bgExecutor.shutdown()
    }
}
