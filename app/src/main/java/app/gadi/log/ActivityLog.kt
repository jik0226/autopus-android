package app.gadi.log

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Entry kinds tracked on the activity log timeline.
 *
 * Stays narrow on purpose — kinds correspond directly to observable
 * user-facing events. Adding a kind is cheap; widening an existing kind
 * is not, so prefer adding a new one.
 */
enum class ActivityLogKind {
    CHAT_USER,
    CHAT_GADI,
    NOTIFICATION_POSTED,
    NOTIFICATION_CLASSIFIED,
    TOOL_USED,
}

data class ActivityLogEntry(
    val timestampMillis: Long,
    val kind: ActivityLogKind,
    val summary: String,
    val detail: String? = null,
)

/**
 * Process-wide in-memory activity log.
 *
 * Holds the most recent [MAX_ENTRIES] entries (FIFO). Subscribed UI
 * receives updates via [StateFlow]. No persistence yet — entries vanish
 * on process kill. Persistence (SQLite or DataStore) is planned for
 * v0.3+ once we know which patterns we actually want to learn from.
 *
 * Privacy: stays on-device. Never serialized off-device.
 */
object ActivityLog {
    private const val MAX_ENTRIES = 200

    private val _entries = MutableStateFlow<List<ActivityLogEntry>>(emptyList())
    val entries: StateFlow<List<ActivityLogEntry>> = _entries.asStateFlow()

    fun add(kind: ActivityLogKind, summary: String, detail: String? = null) {
        val entry = ActivityLogEntry(
            timestampMillis = System.currentTimeMillis(),
            kind = kind,
            summary = summary,
            detail = detail,
        )
        // Newest first.
        _entries.value = (listOf(entry) + _entries.value).take(MAX_ENTRIES)
    }

    fun clear() {
        _entries.value = emptyList()
    }
}
