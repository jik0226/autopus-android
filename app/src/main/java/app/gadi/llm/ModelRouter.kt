package app.gadi.llm

import android.content.Context
import app.gadi.tools.defaultPhoneStateTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ModelRouter : AutoCloseable {
    val isOnDevice: Boolean

    suspend fun generate(prompt: String, maxTokens: Int = 256): String

    override fun close() = Unit
}

class LocalRouter(
    private val engine: GemmaInferenceEngine,
) : ModelRouter {
    override val isOnDevice: Boolean = true

    override suspend fun generate(prompt: String, maxTokens: Int): String = withContext(Dispatchers.Default) {
        engine.generate(buildGadiPrompt(prompt = prompt, maxTokens = maxTokens)).trim()
    }

    override fun close() {
        engine.close()
    }

    private fun buildGadiPrompt(prompt: String, maxTokens: Int): String {
        return """
            <start_of_turn>user
            너는 안드로이드 폰 안에 사는 귀여운 모바일 비서 Gadi야.
            원칙:
            - 한국어로 답해.
            - 답변은 짧고 다정하게 해.
            - 지금은 폰 상태 조회 도구가 없으니 시간, 배터리, 알림, 파일 내용은 안다고 꾸미지 마.
            - 사용자가 실제 폰 조작을 요구하면 아직 준비 중이라고 말해.
            - ${maxTokens}토큰 이하로 답해.

            사용자 메시지:
            $prompt
            <end_of_turn>
            <start_of_turn>model
        """.trimIndent()
    }
}

class CloudRouter : ModelRouter {
    override val isOnDevice: Boolean = false

    override suspend fun generate(prompt: String, maxTokens: Int): String {
        TODO("Option B: implement when user toggles cloud fallback")
    }
}

object ModelRouterFactory {
    fun createDefault(context: Context): ModelRouter {
        val app = context.applicationContext
        val engine = GemmaInferenceEngine(app)
        val localRouter = LocalRouter(engine)
        val tools = defaultPhoneStateTools(app)
        val classifier = IntentClassifier(tools)
        return ToolRouter(app, engine, localRouter, classifier)
    }
}
