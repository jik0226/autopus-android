package app.gadi.notification

import android.content.Context
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
class NotificationClassifier(
    private val context: Context,
    private val router: ModelRouter,
) {

    suspend fun classify(
        packageName: String,
        title: String,
        text: String,
    ): NotificationImportance {
        // Per-package user override comes first.
        when (NotificationRulesStore.get(context, packageName)) {
            NotificationRule.ALWAYS_IMPORTANT -> return NotificationImportance.IMPORTANT
            NotificationRule.ALWAYS_NORMAL -> return NotificationImportance.NORMAL
            NotificationRule.IGNORE -> return NotificationImportance.UNKNOWN
            NotificationRule.DEFAULT -> { /* fall through to classifier */ }
        }
        // v0.2 — Rules first. Gemma 1B Korean classification proved unreliable
        // even with few-shot (returned IMPORTANT on 광고/일반 inputs). Keyword
        // rules give deterministic answers within the v0.2 scope. v0.3 will
        // revisit with a larger model or fine-tuning (see DESIGN.md §10).
        val ruleResult = ruleBasedClassify(title, text)
        if (ruleResult != NotificationImportance.UNKNOWN) {
            return ruleResult
        }
        // Fall back to LLM only when rules can't decide. Still on-device.
        return llmClassify(packageName, title, text)
    }

    /**
     * Deterministic keyword-based classification.
     *
     * Returns UNKNOWN when no keyword matches so the caller can try the LLM.
     */
    private fun ruleBasedClassify(title: String, text: String): NotificationImportance {
        val combined = (title + " " + text).lowercase()

        if (IMPORTANT_KEYWORDS.any { it in combined }) {
            return NotificationImportance.IMPORTANT
        }
        if (NORMAL_KEYWORDS.any { it in combined }) {
            return NotificationImportance.NORMAL
        }
        return NotificationImportance.UNKNOWN
    }

    private suspend fun llmClassify(
        packageName: String,
        title: String,
        text: String,
    ): NotificationImportance {
        val prompt = buildPrompt(packageName, title, text)
        val response = router.generate(prompt, maxTokens = 8).trim().lowercase()
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

    private companion object {
        // Korean + English signals for the rule-based pre-pass.
        // Keep these conservative: any false IMPORTANT for an ad costs the user
        // more attention than a false NORMAL for one missed meeting.
        val IMPORTANT_KEYWORDS = listOf(
            // family & people
            "엄마", "아빠", "형", "누나", "언니", "동생", "오빠", "가족",
            "선생님", "부장", "팀장", "사장", "고객",
            // work / schedule
            "미팅", "회의", "회사", "일정", "약속", "알람", "긴급", "급함",
            "마감", "출발", "도착",
            // English
            "urgent", "asap", "meeting", "deadline",
        )
        val NORMAL_KEYWORDS = listOf(
            // ads & marketing
            "광고", "할인", "이벤트", "특가", "쿠폰", "세일", "프로모션", "혜택",
            // automated / completion
            "배송", "주문 완료", "결제 완료", "처리 완료", "구독", "포인트",
            // system / generic
            "시스템", "업데이트", "안내", "공지",
            // English
            "ad", "promotion", "sale", "discount", "offer", "coupon",
        )
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
