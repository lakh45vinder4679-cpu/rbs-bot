package com.lakhvinder.rbsbot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lakhvinder.rbsbot.RbsApp
import com.lakhvinder.rbsbot.data.local.AiProvider
import com.lakhvinder.rbsbot.data.local.AppLanguage
import com.lakhvinder.rbsbot.data.local.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() = getApplication<RbsApp>()

    val settings: StateFlow<UserSettings> = app.settingsRepository.settings.stateIn(
        viewModelScope, SharingStarted.Eagerly, UserSettings()
    )

    fun setLanguage(lang: AppLanguage) = viewModelScope.launch { app.settingsRepository.setLanguage(lang) }

    fun setProvider(provider: AiProvider) = viewModelScope.launch { app.settingsRepository.setProvider(provider) }

    fun setGeminiKey(key: String) = viewModelScope.launch { app.settingsRepository.setGeminiKey(key) }

    fun setGeminiModel(model: String) = viewModelScope.launch { app.settingsRepository.setGeminiModel(model) }

    fun setGeminiGrounding(on: Boolean) = viewModelScope.launch { app.settingsRepository.setGeminiGrounding(on) }

    fun setOpenRouterKey(key: String) = viewModelScope.launch { app.settingsRepository.setOpenRouterKey(key) }

    fun setOpenRouterModel(model: String) = viewModelScope.launch { app.settingsRepository.setOpenRouterModel(model) }

    fun setEducationMode(on: Boolean) = viewModelScope.launch { app.settingsRepository.setEducationMode(on) }

    fun setFreeOnly(on: Boolean) = viewModelScope.launch { app.settingsRepository.setFreeOnly(on) }

    fun setAutoModel(on: Boolean) = viewModelScope.launch { app.settingsRepository.setAutoModel(on) }

    fun setShowModel(on: Boolean) = viewModelScope.launch { app.settingsRepository.setShowModel(on) }

    fun setIndianContext(on: Boolean) = viewModelScope.launch { app.settingsRepository.setIndianContext(on) }

    fun setFallback(on: Boolean) = viewModelScope.launch { app.settingsRepository.setFallback(on) }
}
