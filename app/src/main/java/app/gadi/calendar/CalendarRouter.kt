package app.gadi.calendar

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Maps Calendar-shaped chat prompts to repository actions.
 *
 * v0.3a.3 (this commit): handles "add" intent only ("내일 영희랑 7시 약속")
 *   - CalendarIntentExtractor parses the prompt into title/start/end.
 *   - CalendarRepository inserts into the user's default writable calendar.
 *   - Returns a Korean confirmation or a graceful error string for the
 *     chat bubble; never throws.
 *
 * v0.3a.4 will add the "list" path ("내일 일정 뭐 있어?") and a wider
 * intent disambiguator. v0.3a.5 adds runtime-permission onboarding so
 * the hasWritePermission() check resolves true on first run.
 *
 * Privacy: stays fully on-device — extractor is rule-based, repository
 * uses the system CalendarContract provider only.
 */
class CalendarRouter(context: Context) {

    private val repo = CalendarRepository(context.applicationContext)
    private val timeFormat = SimpleDateFormat("M월 d일 EEEE a h시 m분", Locale.KOREAN)

    /**
     * Run the calendar pipeline against [prompt]. Always returns a string
     * suitable for the chat bubble — error states have human-readable copy.
     */
    suspend fun handle(prompt: String): String {
        return handleAdd(prompt)
    }

    private fun handleAdd(prompt: String): String {
        val intent = CalendarIntentExtractor.extract(prompt)
            ?: return "일정을 이해하지 못했어요. 예: \"내일 오후 7시 영희랑 약속\""

        if (!repo.hasWritePermission()) {
            return "캘린더에 일정을 추가하려면 권한이 필요해요. 설정에서 캘린더 접근을 허용해 주세요."
        }
        val calendarId = repo.defaultWritableCalendarId()
            ?: return "쓸 수 있는 캘린더를 찾지 못했어요. 디바이스에 캘린더를 추가하거나 동기화를 확인해 주세요."

        val eventId = repo.addEvent(
            calendarId = calendarId,
            title = intent.title,
            startMillis = intent.startMillis,
            endMillis = intent.endMillis,
        )
        if (eventId == null) {
            Log.w(TAG, "addEvent returned null calendarId=$calendarId title=${intent.title}")
            return "일정 등록에 실패했어요. 잠시 후 다시 시도해 주세요."
        }

        val timeText = timeFormat.format(Date(intent.startMillis))
        return "${intent.title} 일정 등록했어요 — $timeText."
    }

    private companion object {
        const val TAG = "GadiCalendarRouter"
    }
}

/**
 * Keyword-based gate for Calendar prompts. Lives next to [CalendarRouter]
 * because the keyword list and the router evolve together.
 */
object CalendarKeywords {
    private val KEYWORDS = listOf(
        "약속", "미팅", "일정", "스케줄", "회의", "예약",
        "calendar", "schedule",
    )

    fun match(input: String): Boolean {
        val lower = input.lowercase()
        return KEYWORDS.any { it in lower }
    }
}
