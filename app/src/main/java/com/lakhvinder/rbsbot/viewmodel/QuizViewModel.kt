package com.lakhvinder.rbsbot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lakhvinder.rbsbot.RbsApp
import com.lakhvinder.rbsbot.data.local.BookmarkEntity
import com.lakhvinder.rbsbot.data.remote.AiApiException
import com.lakhvinder.rbsbot.data.remote.AiClient
import com.lakhvinder.rbsbot.model.Mcq
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray

data class QuizUiState(
    val subject: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val questions: List<Mcq> = emptyList(),
    val index: Int = 0,
    val selected: Int? = null,
    val answered: Boolean = false,
    val showExplain: Boolean = false,
    val score: Int = 0,
    val finished: Boolean = false,
    val actionMessage: String? = null
) {
    val current: Mcq? get() = questions.getOrNull(index)
    val progress: Int get() = index + 1
}

class QuizViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app get() = getApplication<RbsApp>()
    val subject: String = savedStateHandle["subject"] ?: "General"

    private val _state = MutableStateFlow(QuizUiState(subject = subject, loading = true))
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        _state.update { QuizUiState(subject = subject, loading = true) }
        viewModelScope.launch {
            try {
                val settings = app.settingsRepository.settings.first()
                if (!settings.isReady()) throw AiApiException("Pehle Settings me API key daalo - Gemini ya OpenRouter.")
                val qs = app.aiClient.fetchMcqs(settings, subject)
                if (qs.size < 4) throw AiApiException("AI ne kaafi questions nahi diye. Retry karo.")
                _state.update { it.copy(loading = false, questions = qs) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = friendly(e)) }
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        _state.update { s ->
            if (s.answered || s.loading || s.questions.isEmpty()) s
            else s.copy(
                selected = optionIndex,
                answered = true,
                showExplain = true,
                score = s.score + if (optionIndex == s.current?.answerIndex) 1 else 0
            )
        }
    }

    fun closeExplain() = _state.update { it.copy(showExplain = false) }

    fun bookmarkCurrent() {
        val q = _state.value.current ?: return
        if (!_state.value.answered) return
        viewModelScope.launch {
            try {
                val opts = JSONArray().apply { q.options.forEach { put(it) } }
                app.bookmarkDao.deleteByQuestion(q.question)
                app.bookmarkDao.insert(
                    BookmarkEntity(
                        subject = subject,
                        question = q.question,
                        optionsJson = opts.toString(),
                        answerIndex = q.answerIndex,
                        explanation = q.explanation,
                        timestamp = System.currentTimeMillis()
                    )
                )
                _state.update { it.copy(actionMessage = "Bookmark save ho gaya") }
            } catch (e: Exception) {
                _state.update { it.copy(actionMessage = "Bookmark save nahi hua") }
            }
        }
    }

    fun nextQuestion() {
        _state.update { s ->
            val last = s.index >= s.questions.size - 1
            if (last) s.copy(showExplain = false, finished = true)
            else s.copy(index = s.index + 1, selected = null, answered = false, showExplain = false)
        }
    }

    fun restart() = _state.update { s -> s.copy(index = 0, selected = null, answered = false, showExplain = false, score = 0, finished = false) }

    fun consumeMessage() = _state.update { it.copy(actionMessage = null) }

    private fun friendly(e: Exception): String = when (e) {
        is AiApiException -> e.message ?: "AI error"
        is java.net.UnknownHostException -> "Internet nahi hai. Connection check karo."
        is java.net.SocketTimeoutException -> "AI ko time laga (timeout). Retry karo."
        else -> e.message ?: "Unexpected error"
    }
}
