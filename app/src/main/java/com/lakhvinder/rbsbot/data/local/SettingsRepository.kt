package com.lakhvinder.rbsbot.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AiProvider(val stored: String) {
    GEMINI("gemini"),
    OPENROUTER("openrouter")
}

enum class AppLanguage(val stored: String, val nativeName: String) {
    HINDI("hi", "हिंदी"),
    ENGLISH("en", "English")
}

data class UserSettings(
    val language: AppLanguage = AppLanguage.HINDI,
    val provider: AiProvider = AiProvider.GEMINI,
    val geminiKey: String = "",
    val geminiModel: String = "gemini-1.5-flash",
    val geminiGrounding: Boolean = false,
    val openRouterKey: String = "",
    val openRouterModel: String = "google/gemma-2-9b-it:free"
) {
    fun isReady(): Boolean = when (provider) {
        AiProvider.GEMINI -> geminiKey.isNotBlank()
        AiProvider.OPENROUTER -> openRouterKey.isNotBlank()
    }
}

private val Context.settingsDataStore by preferencesDataStore(name = "rbs_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val PROVIDER = stringPreferencesKey("provider")
        val GEMINI_KEY = stringPreferencesKey("gemini_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val GEMINI_GROUNDING = booleanPreferencesKey("gemini_grounding")
        val OPENROUTER_KEY = stringPreferencesKey("openrouter_key")
        val OPENROUTER_MODEL = stringPreferencesKey("openrouter_model")
    }

    val settings: Flow<UserSettings> = context.settingsDataStore.data.map { p ->
        UserSettings(
            language = if (p[Keys.LANGUAGE] == "en") AppLanguage.ENGLISH else AppLanguage.HINDI,
            provider = if (p[Keys.PROVIDER] == "openrouter") AiProvider.OPENROUTER else AiProvider.GEMINI,
            geminiKey = p[Keys.GEMINI_KEY] ?: "",
            geminiModel = p[Keys.GEMINI_MODEL] ?: "gemini-1.5-flash",
            geminiGrounding = p[Keys.GEMINI_GROUNDING] ?: false,
            openRouterKey = p[Keys.OPENROUTER_KEY] ?: "",
            openRouterModel = p[Keys.OPENROUTER_MODEL] ?: "google/gemma-2-9b-it:free"
        )
    }

    suspend fun setLanguage(lang: AppLanguage) = context.settingsDataStore.edit {
        it[Keys.LANGUAGE] = lang.stored
    }

    suspend fun setProvider(p: AiProvider) = context.settingsDataStore.edit {
        it[Keys.PROVIDER] = p.stored
    }

    suspend fun setGeminiKey(key: String) = context.settingsDataStore.edit { it[Keys.GEMINI_KEY] = key }

    suspend fun setGeminiModel(model: String) = context.settingsDataStore.edit { it[Keys.GEMINI_MODEL] = model }

    suspend fun setGeminiGrounding(on: Boolean) = context.settingsDataStore.edit { it[Keys.GEMINI_GROUNDING] = on }

    suspend fun setOpenRouterKey(key: String) = context.settingsDataStore.edit { it[Keys.OPENROUTER_KEY] = key }

    suspend fun setOpenRouterModel(model: String) = context.settingsDataStore.edit { it[Keys.OPENROUTER_MODEL] = model }
}
