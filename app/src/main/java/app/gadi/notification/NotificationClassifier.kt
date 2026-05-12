package app.gadi.notification

import app.gadi.llm.ModelRouter

/**
 * Importance bucket assigned to an incoming notification by the on-device LLM.
 */
enum class NotificationImportance { IMPORTANT, NORMAL, UNKNOWN }

/**
 * LLM-based notification importance classifier (v0.2b).
 *
 * Minimal binary classifier: feeds the package name, title, and body of an
 * incoming notification to Gemma 1B and asks for a "중요" / "일반" single-token
 * answer. The raw response is parsed by keyword:
 *   - contains "중요" or "important" → IMPORTANT
 *   - contains "일반" or "normal" → NORMAL
 *   - anything else → UNKNOWN (fallback, treat as normal but flag for tuning)
 *
 * Privacy invariant: notification content stays on-device. The router used
 * here is the same on-device Gemma router as chat; never the cloud fallback.
 */
class NotificationClassifier(private val router: ModelRouter) {

    suspend fun classify(
        packageName: String,
        title: String,
        text: String,
    ): NotificationImportance {
        val prompt = buildPrompt(packageName, title, text)
        val response = router.generate(prompt, maxTokens = 16).trim().lowercase()
        return when {
            response.contains("중요") || response.contains("important") -> NotificationImportance.IMPORTANT
            response.contains("일반") || response.contains("normal") -> NotificationImportance.NORMAL
            else -> NotificationImportance.UNKNOWN
        }
    }

    private fun buildPrompt(packageName: String, title: String, text: String): String {
        return """
            <start_of_turn>user
            아래 안드로이드 알림이 사용자에게 즉시 알릴 만큼 중요한지 한 단어로 판단해.

            앱 패키지: $packageName
            제목: $title
            내용: $text

            기준:
            - 가족/지인 메시지, 업무 연락, 일정/알람 = 중요
            - 광고, 자동 알림, 게임 보상, 시스템 공지 = 일반

            "중요" 또는 "일반" 한 단어로만 답해.
            <end_of_turn>
            <start_of_turn>model
        """.trimIndent()
    }
}
