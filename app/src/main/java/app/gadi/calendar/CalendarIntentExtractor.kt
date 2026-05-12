package app.gadi.calendar

import java.util.Calendar
import java.util.regex.Pattern

/**
 * Structured event extracted from a Korean natural-language request.
 *
 * All timestamps are absolute millis in the device's local timezone.
 */
data class CalendarIntent(
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val description: String? = null,
)

/**
 * Korean natural language → [CalendarIntent].
 *
 * v0.3a.2 — rule-based extraction. Handles the high-frequency patterns
 * that show up in builder dogfooding:
 *
 *   day:  오늘 / 내일 / 모레 / 글피
 *   time: (오전|오후|아침|점심|저녁|밤|새벽)? X시 (반 | X분)?
 *
 * Returns null when both day and time can't be parsed. v0.3a.3 wires
 * the on-device LLM as a fallback for fuzzier phrasings and for cleaner
 * title extraction.
 *
 * Default event duration: 60 minutes (configurable in [DEFAULT_DURATION_MIN]).
 */
object CalendarIntentExtractor {

    private val DAY_PATTERN = Pattern.compile("(오늘|내일|모레|글피)")
    // group 1: optional period word; group 2: hour digits;
    // group 3: optional minute ("반" or digits)
    private val TIME_PATTERN = Pattern.compile(
        "(오전|오후|아침|점심|저녁|밤|새벽)?\\s*(\\d+)\\s*시(?:\\s*(반|\\d+)\\s*분?)?"
    )
    private val EVENT_NOISE = Regex("(약속|미팅|만남|모임|일정|회의|예약)")
    private val PARTICLES = Regex("(이랑|랑|와|과|에서|에)")
    const val DEFAULT_DURATION_MIN: Long = 60

    fun extract(input: String): CalendarIntent? {
        val day = parseDay(input) ?: return null
        val time = parseTime(input) ?: return null

        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, day.offsetDays)
            set(Calendar.HOUR_OF_DAY, time.hour24)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = cal.timeInMillis
        val endMillis = startMillis + DEFAULT_DURATION_MIN * 60_000L

        val title = extractTitle(input, day.matchedText, time.matchedText)

        return CalendarIntent(
            title = title,
            startMillis = startMillis,
            endMillis = endMillis,
        )
    }

    private fun extractTitle(input: String, dayText: String, timeText: String): String {
        return input
            .replace(dayText, " ")
            .replace(timeText, " ")
            .replace(EVENT_NOISE, " ")
            .replace(PARTICLES, " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { "일정" }
    }

    private fun parseDay(input: String): ParsedDay? {
        val m = DAY_PATTERN.matcher(input)
        if (!m.find()) return null
        val raw = m.group(1) ?: return null
        val offset = when (raw) {
            "오늘" -> 0
            "내일" -> 1
            "모레" -> 2
            "글피" -> 3
            else -> return null
        }
        return ParsedDay(offsetDays = offset, matchedText = raw)
    }

    private fun parseTime(input: String): ParsedTime? {
        val m = TIME_PATTERN.matcher(input)
        if (!m.find()) return null
        val period = m.group(1)
        val hourStr = m.group(2) ?: return null
        val minuteStr = m.group(3)

        var hour = hourStr.toIntOrNull() ?: return null
        if (hour !in 0..23) return null
        val minute = when {
            minuteStr == "반" -> 30
            minuteStr != null -> minuteStr.toIntOrNull()?.takeIf { it in 0..59 } ?: 0
            else -> 0
        }

        // PM expansion: 오후/저녁/밤 X시 (X < 12) → X + 12.
        // "오후 12시" stays at 12 (noon).
        if (period in PM_PERIODS && hour in 1..11) {
            hour += 12
        }

        return ParsedTime(hour24 = hour, minute = minute, matchedText = m.group(0).orEmpty())
    }

    private val PM_PERIODS = setOf("오후", "저녁", "밤")

    private data class ParsedDay(val offsetDays: Int, val matchedText: String)
    private data class ParsedTime(val hour24: Int, val minute: Int, val matchedText: String)
}
