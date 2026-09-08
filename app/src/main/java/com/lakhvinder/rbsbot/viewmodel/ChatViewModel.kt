package com.lakhvinder.rbsbot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lakhvinder.rbsbot.RbsApp
import com.lakhvinder.rbsbot.data.local.ChatMessageEntity
import com.lakhvinder.rbsbot.data.remote.AiApiException
import com.lakhvinder.rbsbot.data.remote.AiClient
import com.lakhvinder.rbsbot.model.ChatTurn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() = getApplication<RbsApp>()

    val messages: StateFlow<List<ChatMessageEntity>> = app.chatDao.observeAll().stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _modelLabel = MutableStateFlow("")
    val modelLabel: StateFlow<String> = _modelLabel.asStateFlow()

    fun sendMessage(text: String) {
        val input = text.trim()
        if (input.isEmpty() || _sending.value) return
        _sending.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                app.chatDao.insert(ChatMessageEntity(role = "user", text = input))
                val history = app.chatDao.observeAll().first().takeLast(12)
                    .map { ChatTurn(it.role, it.text) }
                val settings = app.settingsRepository.settings.first()
                if (!settings.isReady()) throw AiApiException("Pehle Settings me API key daalo - Gemini ya OpenRouter.")
                val system = AiClient.systemPrompt(settings)
                val userPrompt = buildUserPrompt(history)
                val reply = app.aiClient.chat(settings, system, userPrompt)
                app.chatDao.insert(ChatMessageEntity(role = "assistant", text = reply))
                _modelLabel.value =
                    if (settings.showModel) (AiClient.lastUsedModel?.let { "Model: $it" } ?: "") else ""
            } catch (e: Exception) {
                _error.value = friendly(e)
            } finally {
                _sending.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch { app.chatDao.clear() }
    }

    fun consumeError() {
        _error.value = null
    }

    private fun buildUserPrompt(history: List<ChatTurn>): String {
        val sb = StringBuilder()
        val recent = history.dropLast(1)
        if (recent.isNotEmpty()) {
            sb.append("Previous chat (use it for context):\n")
            recent.takeLast(6).forEach { sb.append("${it.role}: ${it.text}\n") }
            sb.append("\n")
        }
        val last = history.lastOrNull()?.text ?: ""
        sb.append("Question: $last")
        return sb.toString()
    }

    private fun friendly(e: Exception): String = when (e) {
        is AiApiException -> e.message ?: "AI error"
        is java.net.UnknownHostException -> "Internet nahi hai. Connection check karo."
        is java.net.SocketTimeoutException -> "AI ko time laga (timeout). Retry karo."
        else -> e.message ?: "Unexpected error"
    }
}
