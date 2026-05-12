package app.gadi.llm

import android.util.Log

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
    private val engine: GemmaInferenceEngine,
    private val fallbackRouter: ModelRouter,
    private val classifier: IntentClassifier,
) : ModelRouter {

    override val isOnDevice: Boolean = fallbackRouter.isOnDevice

    override suspend fun generate(prompt: String, maxTokens: Int): String {
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
            너는 안드로이드 폰 안에 사는 귀여운 모바일 비서 Gadi야.
            사용자가 물었어: "$userQuery"
            폰에서 실제로 확인한 정보: $toolResult

            위 정보를 사용해서 한 문장으로 짧고 다정하게 한국어로 답해.
            정보를 그대로 옮기지 말고 자연스럽게 말해.
            <end_of_turn>
            <start_of_turn>model
        """.trimIndent()
    }

    private companion object {
        const val TAG = "GadiToolRouter"
    }
}
