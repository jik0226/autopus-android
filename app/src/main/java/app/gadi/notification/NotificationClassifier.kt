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
        val response = router.generate(prompt, maxTokens = 8).trim().lowercase()
        // First-occurrence wins: previously "contains(중요)" matched even when
        // the model said "중요한지 일반인지 모르겠어요" → false IMPORTANT.
        val firstChunk = response.take(30)
        val importantAt = sequenceOf("중요", "important")
            .map { firstChunk.indexOf(it) }
            .filter { it >= 0 }
            .minOrNull() ?: Int.MAX_VALUE
        val normalAt = sequenceOf("일반", "normal")
            .map { firstChunk.indexOf(it) }
            .filter { it >= 0 }
            .minOrNull() ?: Int.MAX_VALUE
        return when {
            importantAt == Int.MAX_VALUE && normalAt == Int.MAX_VALUE -> NotificationImportance.UNKNOWN
            importantAt < normalAt -> NotificationImportance.IMPORTANT
            else -> NotificationImportance.NORMAL
        }
    }

    private fun buildPrompt(packageName: String, title: String, text: String): String {
        // Few-shot: small models like Gemma 1B need concrete examples to stay
        // on format. Each example ends with a one-word answer; the live row
        // ends with "답:" so the model's first token is the verdict.
        return """
            <start_of_turn>user
            아래 안드로이드 알림을 "중요" 또는 "일반" 한 단어로만 분류해.

            예시:
            앱: com.kakao.talk, 제목: 엄마, 내용: 언제 와?
            답: 중요

            앱: com.coupang.mobile, 제목: 광고, 내용: 오늘만 30% 할인!
            답: 일반

            앱: com.samsung.android.calendar, 제목: 미팅, 내용: 1시간 후 시작
            답: 중요

            앱: com.facebook.katana, 제목: 누군가 좋아요를 눌렀습니다, 내용: 알림
            답: 일반

            앱: $packageName, 제목: $title, 내용: $text
            답:
            <end_of_turn>
            <start_of_turn>model
        """.trimIndent()
    }
}
