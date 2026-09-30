package com.androidengineers.agent_quickstart_android.config

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.androidengineers.agent_quickstart_android.BuildConfig

/**
 * Persists the backend server URL in SharedPreferences so users can configure it
 * at runtime from Settings — works out-of-the-box on any device (emulator or physical phone).
 */
object ServerUrlPrefs {
    private const val PREFS_NAME = "voxpilot_prefs"
    private const val KEY_SERVER_URL = "server_url"

    // Default LAN IP of the host machine
    const val DEFAULT_LAN_URL = "http://10.120.212.110:8000"
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8000"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getServerUrl(context: Context): String =
        prefs(context).getString(KEY_SERVER_URL, "") ?: ""

    fun setServerUrl(context: Context, url: String) {
        prefs(context).edit().putString(KEY_SERVER_URL, url.trim()).apply()
    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu"))
    }

    /** Returns effective URL: user pref > BuildConfig > smart device default */
    fun effectiveUrl(context: Context): String {
        val userUrl = getServerUrl(context).trim()
        if (userUrl.isNotBlank()) return userUrl

        val buildUrl = try { BuildConfig.QUICKSTART_SERVER_URL.trim() } catch (_: Throwable) { "" }
        if (buildUrl.isNotBlank()) {
            // If on a real device, don't use 10.0.2.2 because it is emulator-only
            if (buildUrl.contains("10.0.2.2") && !isEmulator()) {
                return DEFAULT_LAN_URL
            }
            return buildUrl
        }

        return if (isEmulator()) DEFAULT_EMULATOR_URL else DEFAULT_LAN_URL
    }
}
