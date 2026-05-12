package app.gadi.calendar

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
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
    private val shortTimeFormat = SimpleDateFormat("M/d HH:mm", Locale.KOREAN)

    /**
     * Run the calendar pipeline against [prompt]. Always returns a string
     * suitable for the chat bubble — error states have human-readable copy.
     */
    suspend fun handle(prompt: String): String {
        return if (isListIntent(prompt)) {
            handleList(prompt)
        } else {
            handleAdd(prompt)
        }
    }

    private fun isListIntent(prompt: String): Boolean {
        return LIST_HINTS.any { it in prompt }
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

    private fun handleList(prompt: String): String {
        if (!repo.hasReadPermission()) {
            return "캘린더를 보려면 권한이 필요해요. 설정에서 캘린더 접근을 허용해 주세요."
        }
        val range = parseListRange(prompt)
        val events = repo.listEventsBetween(range.startMillis, range.endMillis)
        if (events.isEmpty()) {
            return "${range.label} 일정이 없어요."
        }
        val body = events.joinToString("\n") { event ->
            val timeText = shortTimeFormat.format(Date(event.startMillis))
            val title = event.title.ifBlank { "(제목 없음)" }
            "• $timeText  $title"
        }
        return "${range.label} 일정 ${events.size}개:\n$body"
    }

    private fun parseListRange(prompt: String): ListRange {
        val now = Calendar.getInstance()
        return when {
            "오늘" in prompt -> ListRange(
                startMillis = atStartOfDay(now).timeInMillis,
                endMillis = atStartOfDay(now).apply { add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis,
                label = "오늘",
            )
            "내일" in prompt -> ListRange(
                startMillis = atStartOfDay(now).apply { add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis,
                endMillis = atStartOfDay(now).apply { add(Calendar.DAY_OF_MONTH, 2) }.timeInMillis,
                label = "내일",
            )
            "모레" in prompt -> ListRange(
                startMillis = atStartOfDay(now).apply { add(Calendar.DAY_OF_MONTH, 2) }.timeInMillis,
                endMillis = atStartOfDay(now).apply { add(Calendar.DAY_OF_MONTH, 3) }.timeInMillis,
                label = "모레",
            )
            "이번 주" in prompt || "이번주" in prompt -> {
                val weekStart = atStartOfWeek(now)
                ListRange(
                    startMillis = weekStart.timeInMillis,
                    endMillis = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }.timeInMillis,
                    label = "이번 주",
                )
            }
            "이번 달" in prompt || "이번달" in prompt -> {
                val monthStart = atStartOfMonth(now)
                ListRange(
                    startMillis = monthStart.timeInMillis,
                    endMillis = (monthStart.clone() as Calendar).apply { add(Calendar.MONTH, 1) }.timeInMillis,
                    label = "이번 달",
                )
            }
            else -> ListRange(
                startMillis = atStartOfDay(now).timeInMillis,
                endMillis = atStartOfDay(now).apply { add(Calendar.DAY_OF_MONTH, 7) }.timeInMillis,
                label = "앞으로 일주일",
            )
        }
    }

    private fun atStartOfDay(reference: Calendar): Calendar {
        return (reference.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun atStartOfWeek(reference: Calendar): Calendar {
        // Korean convention: week starts Monday.
        return atStartOfDay(reference).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    }

    private fun atStartOfMonth(reference: Calendar): Calendar {
        return atStartOfDay(reference).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private data class ListRange(
        val startMillis: Long,
        val endMillis: Long,
        val label: String,
    )

    private companion object {
        const val TAG = "GadiCalendarRouter"
        val LIST_HINTS = listOf(
            "?", "뭐 있", "뭐있", "있어", "있나",
            "알려", "확인", "보여", "조회",
        )
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
