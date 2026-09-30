package com.androidengineers.agent_quickstart_android.model

enum class VoxPilotMode(
    val title: String,
    val subtitle: String,
    val serverKey: String,
    val promptDescription: String,
) {
    ASK(
        title = "Ask",
        subtitle = "General Voice Copilot",
        serverKey = "ask",
        promptDescription = "Ask anything, brainstorm ideas, get direct conversational answers.",
    ),
    LEARN(
        title = "Learn",
        subtitle = "Interactive Tutor",
        serverKey = "learn",
        promptDescription = "Interactive tutor with step-by-step learning, Hindi explanations, cricket examples, and verbal quizzes.",
    ),
    TRANSLATE(
        title = "Translate",
        subtitle = "English ↔ Hindi",
        serverKey = "translate",
        promptDescription = "Real-time bilingual voice translation between English and Hindi/Hinglish.",
    );
}

enum class NavigationTab {
    HOME,
    NOTES,
    HISTORY,
    SETTINGS,
}

enum class LanguagePreference(
    val title: String,
    val serverKey: String,
) {
    AUTO("Auto Detect", "auto"),
    ENGLISH("English", "en"),
    HINDI_HINGLISH("Hindi / Hinglish", "hi");
}
