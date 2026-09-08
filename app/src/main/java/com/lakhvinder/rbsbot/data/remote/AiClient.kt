package com.lakhvinder.rbsbot.data.remote

import com.lakhvinder.rbsbot.data.local.AiProvider
import com.lakhvinder.rbsbot.data.local.AppLanguage
import com.lakhvinder.rbsbot.data.local.UserSettings
import com.lakhvinder.rbsbot.model.Mcq
import org.json.JSONArray
import org.json.JSONObject

/**
 * AI orchestration client. Works 100% with external LLM APIs
 * (Google Gemini via generateContent, OpenRouter via chat/completions).
 * No backend server involved.
 *
 * FREE-ONLY policy (verified Aug/Sep 2026 against the live OpenRouter
 * public model catalogue): this app only ever routes to genuinely free
 * models. Paid models are never auto-switched to.
 */
object AiClient {

    private const val GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models/"
    private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"

    /**
     * Currently-available free-tier Gemini model ids (newest-first).
     * Verified present in the live provider catalogues; Google keeps the
     * current Flash line available on the free API tier.
     */
    val GEMINI_MODELS = listOf(
        "gemini-3.5-flash",
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite",
        "gemini-2.5-flash"
    )

    /**
     * Verified currently-free OpenRouter models that suit EDUCATION
     * (text chat, broad knowledge, reasoning, Hindi/Hinglish capable).
     * Each id was confirmed free (prompt/completion price == 0) in the live
     * public catalogue at build time. Code-only/health/finance/music and
     * guardrail models were deliberately excluded.
     */
    val OPENROUTER_FREE_MODELS = listOf(
        "google/gemma-4-31b-it:free",
        "nvidia/nemotron-3-super-120b-a12b:free",
        "thinkingmachines/inkling:free",
        "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free",
        "dots-studio/dots-3-note-preview:free",
        "nvidia/nemotron-3.5-lightning:free"
    )

    /** OpenRouter fallback order (general -> reasoning -> big), used when a model fails. */
    private val OPENROUTER_FALLBACK_CHAIN = listOf(
        "google/gemma-4-31b-it:free",
        "nvidia/nemotron-3-super-120b-a12b:free",
        "thinkingmachines/inkling:free",
        "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free",
        "dots-studio/dots-3-note-preview:free"
    )

    /** Model actually used for the last successful AI call (for "Show current model"). */
    @Volatile
    var lastUsedModel: String? = null
        private set

    /** Default education routing (used when Auto Model Selection is ON). */
    private const val GENERAL_FREE = "google/gemma-4-31b-it:free"
    private const val REASONING_FREE = "thinkingmachines/inkling:free"
    private const val BIG_FREE = "nvidia/nemotron-3-super-120b-a12b:free"

    fun systemPrompt(settings: UserSettings): String {
        val langLine = when (settings.language) {
            AppLanguage.HINDI ->
                "Language: Always respond in simple Hinglish/Hindi (Devanagari allowed). Write explanations in easy Hindi so a school student understands."
            AppLanguage.ENGLISH ->
                "Language: Always respond in clear, simple English."
        }
        val role = if (settings.educationMode)
            "You are RBS Bot, a friendly personal AI teacher helping students prepare for Rajasthan Board (RBSE) exams. " +
            "Teach step by step: basic -> intermediate -> advanced. Give examples, keep it simple, avoid unnecessary jargon, " +
            "and ask a short clarifying question if the question is ambiguous."
        else
            "You are RBS Bot, a helpful assistant for Rajasthan Board (RBSE) exam preparation."
        val india = if (settings.indianContext)
            "Indian context: answer in the Indian education/culture framework (syllabus, board pattern, Indian examples), and treat Rajasthan-specific history, geography, art & culture, polity and current affairs as priority topics."
        else ""
        val freeNote = "Policy: never recommend or require any paid model — the user runs free models only."
        return "$role $india $freeNote $langLine"
    }

    private fun jsonInstruction(): String =
        "Return ONLY a raw JSON array with NO markdown, NO code fences and NO extra text."

    /* ---------------- Model selection ---------------- */

    /**
     * Pure selection of the model id that will be used for a given request.
     * Returns the resolved model so the UI can show "current model".
     */
    fun resolveModel(settings: UserSettings, routeText: String = ""): String {
        return when (settings.provider) {
            AiProvider.GEMINI -> settings.geminiModel
            AiProvider.OPENROUTER -> if (settings.autoModel) autoRouteOr(routeText) else settings.openRouterModel
        }
    }

    private fun autoRouteOr(routeText: String): String {
        val t = routeText.lowercase()
        val math = arrayOf("math", "maths", "calculate", "solve", "equation", "geometry", "algebra",
            "percentage", "trigonometry", "fraction", "ratio", "sum", "ganit", "hisab", "sawal",
            "add", "subtract", "multiply", "divide", "sin", "cos", "log")
        if (math.any { t.contains(it) }) return REASONING_FREE
        val hard = arrayOf("difficult", "complex", "tough", "hard", "kathin", "advanced", "reason", "prove", "derive")
        if (hard.any { t.contains(it) }) return BIG_FREE
        // Long current-affairs / factual pillar questions -> big broad-knowledge model.
        return GENERAL_FREE
    }

    /* ---------------- Public chat ---------------- */

    suspend fun chat(settings: UserSettings, system: String, user: String): String =
        when (settings.provider) {
            AiProvider.GEMINI ->
                gemini(settings, system, user, settings.geminiModel)
            AiProvider.OPENROUTER -> {
                val model = resolveModel(settings, user)
                openRouter(settings, system, user, model)
            }
        }

    /* ---------------- MCQ generation ---------------- */

    suspend fun fetchMcqs(settings: UserSettings, subject: String): List<Mcq> {
        val system = systemPrompt(settings) + " You are also a Rajasthan exam paper setter."
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

    private suspend fun gemini(settings: UserSettings, system: String, user: String, requested: String): String {
        // Fallback chain only when enabled and the model id itself is rejected.
        val chain = if (settings.fallback) {
            val from = GEMINI_MODELS.indexOf(requested)
            listOf(requested) + GEMINI_MODELS.filterIndexed { i, _ -> i != from }
        } else listOf(requested)

        var lastErr: AiApiException? = null
        for (model in chain.take(3)) {
            try {
                return geminiOne(settings, system, user, model)
            } catch (e: AiApiException) {
                if (!modelSwitchable(e)) throw e
                lastErr = e
            }
        }
        throw lastErr ?: AiApiException("Gemini call failed.")
    }

    private suspend fun geminiOne(settings: UserSettings, system: String, user: String, model: String): String {
        val encoded = java.net.URLEncoder.encode(model, "UTF-8")
        val url = "${GEMINI_BASE}${encoded}:generateContent?key=${settings.geminiKey}"

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
        val text = parseGemini(resp)
        lastUsedModel = model
        return text
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

    private suspend fun openRouter(settings: UserSettings, system: String, user: String, requested: String): String {
        val chain = if (settings.fallback) {
            val from = OPENROUTER_FALLBACK_CHAIN.indexOf(requested)
            listOf(requested) + OPENROUTER_FALLBACK_CHAIN.filterIndexed { i, _ -> i != from }
        } else listOf(requested)

        var lastErr: AiApiException? = null
        for (model in chain.take(3)) {
            try {
                return openRouterOne(settings, system, user, model)
            } catch (e: AiApiException) {
                if (!modelSwitchable(e)) throw e
                lastErr = e
            }
        }
        throw lastErr ?: AiApiException("OpenRouter call failed.")
    }

    private suspend fun openRouterOne(settings: UserSettings, system: String, user: String, model: String): String {
        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", system))
            .put(JSONObject().put("role", "user").put("content", user))
        val root = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", 0.7)

        val body = ApiFactory.jsonBody(root.toString())
        val auth = "Bearer ${settings.openRouterKey}"
        val resp = ApiFactory.api.post(OPENROUTER_URL, auth, "RBS Bot", body)
        val text = parseOpenRouter(resp)
        lastUsedModel = model
        return text
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

    /** True when the failure is a "model not found / over quota / server" problem worth trying another model for. */
    private fun modelSwitchable(e: AiApiException): Boolean {
        val msg = (e.message ?: "").lowercase()
        return e.code == 404 || e.code == 429 || e.code in 500..599 ||
            msg.contains("model") || msg.contains("not found") || msg.contains("does not exist")
    }

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
            404 -> "Model ya endpoint nahi mila. Settings me model check karo (fallback try hua)."
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
