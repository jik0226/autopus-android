package app.gadi.llm

import android.content.Context
import android.util.Log
import app.gadi.calendar.CalendarKeywords
import app.gadi.calendar.CalendarRouter

/**
 * Routes user input through tool detection then LLM phrasing.
 *
 * Flow:
 *   1. [IntentClassifier] picks a Tool by keyword.
 *   2. If matched: tool.execute() → phrasingPrompt → engine.generate().
 *   3. If not matched: fall through to [fallbackRouter] (plain Gadi chat).
 *
 * Owns no model resources — [fallbackRouter] retains ownership of [engine]
 * and will close it.
 */
class ToolRouter(
    private val context: Context,
    private val engine: GemmaInferenceEngine,
    private val fallbackRouter: ModelRouter,
    private val classifier: IntentClassifier,
) : ModelRouter {

    override val isOnDevice: Boolean = fallbackRouter.isOnDevice

    private val calendarRouter = CalendarRouter(context)

    override suspend fun generate(prompt: String, maxTokens: Int): String {
        // Calendar-shaped prompts route first; their output is already
        // human-readable so no LLM phrasing pass is needed.
        if (CalendarKeywords.match(prompt)) {
            return calendarRouter.handle(prompt)
        }

        val tool = classifier.classify(prompt)
            ?: return fallbackRouter.generate(prompt, maxTokens)

        val toolResult = try {
            tool.execute()
        } catch (t: Throwable) {
            Log.w(TAG, "Tool ${tool.name} failed; falling back to plain chat", t)
            return fallbackRouter.generate(prompt, maxTokens)
        }

        Log.i(TAG, "Tool=${tool.name} result=$toolResult")
        val phrasingPrompt = buildPhrasingPrompt(prompt, toolResult)
        return engine.generate(phrasingPrompt).trim()
    }

    override fun close() {
        // Engine ownership belongs to fallbackRouter (LocalRouter); close once via the fallback.
        fallbackRouter.close()
    }

    private fun buildPhrasingPrompt(userQuery: String, toolResult: String): String {
        return """
            <start_of_turn>user
            너는 안드로이드 폰 안에 사는 귀여운 비서 Gadi야.

            사용자 질문: "$userQuery"
            폰에서 방금 측정한 정확한 값: $toolResult

            규칙:
            - 위에 적힌 정확한 값을 반드시 그대로 사용해.
            - 다른 시간, 숫자, 상태를 절대 만들어내지 마.
            - 한국어로 한 문장으로 짧고 다정하게 답해.
            <end_of_turn>
            <start_of_turn>model
        """.trimIndent()
    }

    private companion object {
        const val TAG = "GadiToolRouter"
    }
}
