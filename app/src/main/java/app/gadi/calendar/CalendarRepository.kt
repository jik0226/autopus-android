package app.gadi.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.TimeZone

/**
 * Read/write wrapper around CalendarContract.* for v0.3a.
 *
 * Targets the device-synced calendars (Google, Naver, Samsung, Toss, etc.)
 * via the standard system provider — no per-vendor SDKs, no network. All
 * data stays on-device.
 *
 * Permissions required at runtime: READ_CALENDAR (list) +
 * WRITE_CALENDAR (insert). Methods return null/empty on permission
 * denial rather than throwing, so the caller (Tool layer) can fall back
 * to "권한이 필요해요" messaging instead of crashing.
 */
data class CalendarAccount(
    val id: Long,
    val displayName: String,
    val accountName: String,
)

data class CalendarEvent(
    val id: Long,
    val calendarId: Long,
    val title: String,
    val description: String?,
    val startMillis: Long,
    val endMillis: Long,
)

class CalendarRepository(private val context: Context) {

    fun hasReadPermission(): Boolean =
        checkPerm(Manifest.permission.READ_CALENDAR)

    fun hasWritePermission(): Boolean =
        checkPerm(Manifest.permission.WRITE_CALENDAR)

    private fun checkPerm(name: String): Boolean =
        ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED

    /**
     * Lists all calendar accounts visible to the app (including read-only ones).
     */
    fun listCalendars(): List<CalendarAccount> {
        if (!hasReadPermission()) return emptyList()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
        )
        val result = mutableListOf<CalendarAccount>()
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                result += CalendarAccount(
                    id = cursor.getLong(0),
                    displayName = cursor.getString(1).orEmpty(),
                    accountName = cursor.getString(2).orEmpty(),
                )
            }
        }
        return result
    }

    /**
     * Picks a writable calendar ID for inserts. Heuristic:
     *   1. Primary calendar with at least CONTRIBUTOR access.
     *   2. Otherwise, the first calendar with CONTRIBUTOR+ access.
     *   3. null if no writable calendar exists.
     */
    fun defaultWritableCalendarId(): Long? {
        if (!hasReadPermission()) return null
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
        )
        val selection = "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
        val selectionArgs = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Calendars.IS_PRIMARY} DESC",
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
        return null
    }

    /**
     * Inserts an event. Returns the new event ID or null on failure
     * (permission missing, calendar gone, etc.).
     */
    fun addEvent(
        calendarId: Long,
        title: String,
        startMillis: Long,
        endMillis: Long,
        description: String? = null,
    ): Long? {
        if (!hasWritePermission()) return null
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            if (description != null) put(CalendarContract.Events.DESCRIPTION, description)
        }
        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.let { ContentUris.parseId(it) }
        } catch (t: Throwable) {
            Log.w(TAG, "addEvent failed", t)
            null
        }
    }

    /**
     * Lists events whose start time falls within `[startMillis, endMillis)`,
     * across all visible calendars, ordered by start time ascending.
     */
    fun listEventsBetween(startMillis: Long, endMillis: Long): List<CalendarEvent> {
        if (!hasReadPermission()) return emptyList()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
        )
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"
        val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())
        val result = mutableListOf<CalendarEvent>()
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC",
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                result += CalendarEvent(
                    id = cursor.getLong(0),
                    calendarId = cursor.getLong(1),
                    title = cursor.getString(2).orEmpty(),
                    description = cursor.getString(3),
                    startMillis = cursor.getLong(4),
                    endMillis = cursor.getLong(5),
                )
            }
        }
        return result
    }

    private companion object {
        const val TAG = "GadiCalendarRepo"
    }
}
