package com.ace.jp.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

data class AppSettings(
    val katakanaFreeMode: Boolean = false,
    val allMeaningsRequired: Boolean = false,
    val sensesRequired: Boolean = false,
    val dualMode: Boolean = false
)

class SettingsManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("jpa_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        AppSettings(
            katakanaFreeMode = prefs.getBoolean(KEY_KATAKANA_FREE_MODE, false),
            allMeaningsRequired = prefs.getBoolean(KEY_ALL_MEANINGS_REQUIRED, false),
            sensesRequired = prefs.getBoolean(KEY_SENSES_REQUIRED, false),
            dualMode = prefs.getBoolean(KEY_DUAL_MODE, false)
        )
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setKatakanaFreeMode(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_KATAKANA_FREE_MODE, enabled) }
        _settings.value = _settings.value.copy(katakanaFreeMode = enabled)
    }

    fun setAllMeaningsRequired(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ALL_MEANINGS_REQUIRED, enabled) }
        _settings.value = _settings.value.copy(allMeaningsRequired = enabled)
    }

    fun setSensesRequired(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SENSES_REQUIRED, enabled) }
        _settings.value = _settings.value.copy(sensesRequired = enabled)
    }

    fun setDualMode(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DUAL_MODE, enabled) }
        _settings.value = _settings.value.copy(dualMode = enabled)
    }

    companion object {
        private const val KEY_KATAKANA_FREE_MODE = "katakana_free_mode"
        private const val KEY_ALL_MEANINGS_REQUIRED = "all_meanings_required"
        private const val KEY_SENSES_REQUIRED = "senses_required"
        private const val KEY_DUAL_MODE = "dual_mode"
    }
}