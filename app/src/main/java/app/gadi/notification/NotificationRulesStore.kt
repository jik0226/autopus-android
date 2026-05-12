package app.gadi.notification

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Per-package classification override applied before the keyword/LLM
 * classifier runs.
 */
enum class NotificationRule {
    /** Run the normal classifier (rule keywords + LLM fallback). */
    DEFAULT,

    /** Always treat as IMPORTANT — bubble up regardless of content. */
    ALWAYS_IMPORTANT,

    /** Always treat as NORMAL — log but never bubble up. */
    ALWAYS_NORMAL,

    /** Drop entirely — don't log, don't classify, don't surface. */
    IGNORE,
}

/**
 * Per-package classification overrides, persisted in SharedPreferences.
 *
 * Process-wide singleton. Methods take [Context] to avoid baking in a
 * static Application reference; reads are cheap (mmap-backed prefs).
 *
 * Writes bump [version] so UI subscribers re-fetch via [all]. Setting a
 * package back to DEFAULT removes the entry to keep the prefs file clean.
 *
 * Privacy: stays on-device. Never transmitted off-device.
 */
object NotificationRulesStore {
    private const val PREFS = "gadi_notification_rules"

    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    fun get(context: Context, packageName: String): NotificationRule {
        val raw = prefs(context).getString(packageName, null) ?: return NotificationRule.DEFAULT
        return runCatching { NotificationRule.valueOf(raw) }.getOrDefault(NotificationRule.DEFAULT)
    }

    fun set(context: Context, packageName: String, rule: NotificationRule) {
        prefs(context).edit().apply {
            if (rule == NotificationRule.DEFAULT) {
                remove(packageName)
            } else {
                putString(packageName, rule.name)
            }
            apply()
        }
        _version.value = _version.value + 1
    }

    fun all(context: Context): Map<String, NotificationRule> {
        return prefs(context).all.mapNotNull { (key, value) ->
            val rule = (value as? String)?.let {
                runCatching { NotificationRule.valueOf(it) }.getOrNull()
            }
            if (rule != null && rule != NotificationRule.DEFAULT) key to rule else null
        }.toMap()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}
