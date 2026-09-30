package com.androidengineers.agent_quickstart_android.config

import com.androidengineers.agent_quickstart_android.BuildConfig

object QuickstartConfig {
    var runtimeBaseUrl: String? = null

    val backendBaseUrl: String
        get() = runtimeBaseUrl?.trim()?.trimEnd('/')?.takeIf { it.isNotBlank() }
            ?: runCatching { BuildConfig.QUICKSTART_SERVER_URL.trim().trimEnd('/') }.getOrDefault("")

    fun missingRequiredValues(): List<String> = emptyList()

    // Always configured: VoxPilot can run with companion server OR standalone on any device
    val isConfigured: Boolean
        get() = true

    fun startupHelpMessage(): String? = null
}
