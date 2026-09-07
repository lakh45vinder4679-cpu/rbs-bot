package com.lakhvinder.rbsbot.data.remote

import com.lakhvinder.rbsbot.data.local.AppLanguage
import com.lakhvinder.rbsbot.data.local.UserSettings
import com.lakhvinder.rbsbot.model.Mcq
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * AI orchestration client. Works 100% with external LLM APIs
 * (Google Gemini via generateContent, OpenRouter via chat/completions).
 * No backend server involved.
 */
object AiClient {

    private const val GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models/"
    private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"

    val GEMINI_MODELS = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash")
    val OPENROUTER_FREE_MODELS = listOf(
        "google/gemma-2-9b-it:free",
        "meta-llama/llama-3.1-8b-instruct:free",
        "mistralai/mistral-7b-instruct:free",
        "openchat/openchat-7b:free"
    )

    fun systemPrompt(language: AppLanguage): String {
        val langLine = when (language) {
            AppLanguage.HINDI ->
                "Language: Always respond in simple Hinglish/Hindi (Devanagari allowed). Write explanations in easy Hindi so a school student understands."
            AppLanguage.ENGLISH ->
                "Language: Always respond in clear, simple English."
        }
        return "You are RBS Bot, a friendly AI teacher helping students prepare for Rajasthan Board (RBSE) exams. " +
            "You teach History, Geography, Art & Culture, Polity and Current Affairs of Rajasthan. " +
            "Give accurate, exam-focused answers. $langLine"
    }

    private fun jsonInstruction(): String =
        "Return ONLY a raw JSON array with NO markdown, NO code fences and NO extra text."

    /* ---------------- Public chat ---------------- */

    suspend fun chat(settings: UserSettings, system: String, user: String): String =
        when (settings.provider) {
            com.lakhvinder.rbsbot.data.local.AiProvider.GEMINI ->
                gemini(settings, system, user)
            com.lakhvinder.rbsbot.data.local.AiProvider.OPENROUTER ->
                openRouter(settings, system, user)
        }

    /* ---------------- MCQ generation ---------------- */

    suspend fun fetchMcqs(settings: UserSettings, subject: String): List<Mcq> {
        val system = systemPrompt(settings.language) + " You are also a Rajasthan exam paper setter."
        val user =
            "Generate exactly 10 multiple-choice questions on: \"$subject\". " +
            "Each item is {\"q\":\"question text\",\"options\":[\"a\",\"b\",\"c\",\"d\"],\"a\":0..3 (index of correct option),\"e\":\"short explanation\"}. " +
            "${jsonInstruction()}"
        val raw = chat(settings, system, user)
        val arr = extractJsonArray(raw) ?: throw AiApiException("AI response was not valid JSON. Retry please.")
        val list = ArrayList<Mcq>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val q = o.optString("q").trim()
            val optsArr = o.optJSONArray("options") ?: continue
            if (q.isEmpty() || optsArr.length() < 2) continue
            val opts = ArrayList<String>()
            for (j in 0 until optsArr.length()) opts.add(optsArr.optString(j))
            val answer = o.optInt("a", 0).coerceIn(0, opts.size - 1)
            list.add(Mcq(q, opts, answer, o.optString("e")))
        }
        if (list.isEmpty()) throw AiApiException("AI returned 0 valid questions. Retry please.")
        return list
    }

    /* ---------------- Gemini ---------------- */

    private suspend fun gemini(settings: UserSettings, system: String, user: String): String {
        val model = java.net.URLEncoder.encode(settings.geminiModel, "UTF-8")
        val url = "${GEMINI_BASE}${model}:generateContent?key=${settings.geminiKey}"

        val parts = JSONArray().put(JSONObject().put("text", user))
        val contents = JSONArray().put(JSONObject().put("role", "user").put("parts", parts))
        val root = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
            .put("contents", contents)
            .put("generationConfig", JSONObject().put("temperature", 0.7).put("maxOutputTokens", 4096))
        if (settings.geminiGrounding) {
            root.put("tools", JSONArray().put(JSONObject().put("googleSearch", JSONObject())))
        }

        val body = ApiFactory.jsonBody(root.toString())
        val resp = ApiFactory.api.post(url, null, "RBS Bot", body)
        return parseGemini(resp)
    }

    private fun parseGemini(resp: retrofit2.Response<okhttp3.ResponseBody>): String {
        if (!resp.isSuccessful) throw errorFromBody(resp.code(), resp.errorBody()?.string())
        val text = resp.body()?.string() ?: throw AiApiException("Empty response from Gemini.")
        val root = try { JSONObject(text) } catch (t: Exception) { throw AiApiException("Invalid response from Gemini.") }
        val candidates = root.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val content = candidates.optJSONObject(0)?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null) {
                val sb = StringBuilder()
                for (i in 0 until parts.length()) sb.append(parts.optJSONObject(i).optString("text"))
                if (sb.isNotBlank()) return sb.toString().trim()
            }
        }
        val feedback = root.optJSONObject("promptFeedback")
        val reason = feedback?.optString("blockReason", "")
        throw AiApiException(if (reason.isNullOrEmpty()) "Gemini returned no content. Try again." else "Request blocked by Gemini ($reason).")
    }

    /* ---------------- OpenRouter ---------------- */

    private suspend fun openRouter(settings: UserSettings, system: String, user: String): String {
        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", system))
            .put(JSONObject().put("role", "user").put("content", user))
        val root = JSONObject()
            .put("model", settings.openRouterModel)
            .put("messages", messages)
            .put("temperature", 0.7)

        val body = ApiFactory.jsonBody(root.toString())
        val auth = "Bearer ${settings.openRouterKey}"
        val resp = ApiFactory.api.post(OPENROUTER_URL, auth, "RBS Bot", body)
        return parseOpenRouter(resp)
    }

    private fun parseOpenRouter(resp: retrofit2.Response<okhttp3.ResponseBody>): String {
        if (!resp.isSuccessful) throw errorFromBody(resp.code(), resp.errorBody()?.string())
        val text = resp.body()?.string() ?: throw AiApiException("Empty response from OpenRouter.")
        val root = try { JSONObject(text) } catch (t: Exception) { throw AiApiException("Invalid response from OpenRouter.") }
        val error = root.optJSONObject("error")
        if (error != null) throw AiApiException(error.optString("message", "OpenRouter API error."), error.optInt("code", -1))
        val choices = root.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val msg = choices.optJSONObject(0)?.optJSONObject("message")
            val content = msg?.optString("content", "").orEmpty().trim()
            if (content.isNotEmpty()) return content
        }
        throw AiApiException("OpenRouter returned no content. Try again.")
    }

    /* ---------------- Helpers ---------------- */

    private fun errorFromBody(code: Int, body: String?): AiApiException {
        var message = ""
        try {
            val b = body.orEmpty()
            val root = if (b.trim().startsWith("{")) JSONObject(b) else null
            message = root?.optString("message", "") ?: ""
            if (message.isEmpty()) {
                message = root?.optJSONObject("error")?.optString("message", "") ?: ""
            }
            if (message.isEmpty() && body != null && body.length < 400) message = body.trim()
        } catch (ignored: Exception) {
        }
        val friendly = when (code) {
            400 -> if (message.contains("key", true)) "API key invalid/empty. Settings me key check karo." else "Bad request. Thoda alag try karo."
            401, 403 -> "API key invalid ya blocked. Settings me sahi key daalo."
            404 -> "Model ya endpoint nahi mila. Settings me model check karo."
            429 -> "Rate limit hit - free plan ka limit. Kuch der baad try karo."
            in 500..599 -> "AI server busy hai. Thodi der baad try karo."
            else -> null
        }
        return AiApiException(friendly ?: (if (message.isNotBlank()) message else "API error (HTTP $code)"), code)
    }

    private fun extractJsonArray(text: String): JSONArray? {
        var s = text.trim()
        val fence = s.indexOf("```")
        if (fence >= 0) s = s.substring(fence + 3).trim()
        val start = s.indexOf('[')
        val end = s.lastIndexOf(']')
        if (start < 0 || end <= start) return null
        return try { JSONArray(s.substring(start, end + 1)) } catch (t: Exception) { null }
    }
}
