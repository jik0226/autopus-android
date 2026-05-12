package app.gadi.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import app.gadi.llm.ModelRouter
import app.gadi.llm.ModelRouterFactory
import app.gadi.log.ActivityLog
import app.gadi.log.ActivityLogKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * v0.2 — System-wide notification listener.
 *
 * Receives notification events once the user grants access in
 * Settings → Notifications → Gadi (BIND_NOTIFICATION_LISTENER_SERVICE is a
 * system permission granted only via that special settings toggle).
 *
 * v0.2a: log all posted/removed notifications.
 * v0.2b: classify each posted notification as 중요/일반 via on-device LLM
 *        and log the result. UI surfacing follows in v0.2c, per-package
 *        allow/block list in v0.2d.
 *
 * Privacy invariant: notification content stays on-device. Classification
 * runs through the on-device Gemma router; never the cloud fallback.
 */
class GadiNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var router: ModelRouter? = null
    private var classifier: NotificationClassifier? = null

    override fun onCreate() {
        super.onCreate()
        val r = ModelRouterFactory.createDefault(this)
        router = r
        classifier = NotificationClassifier(r)
        Log.i(TAG, "Service created; classifier ready")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.i(TAG, "Notification listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val pkg = sbn.packageName ?: "unknown"

        // Skip Gadi's own notifications (foreground service FGS notif, debug toasts).
        if (pkg == packageName) return

        val extras = sbn.notification?.extras
        val title = extras?.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras?.getCharSequence("android.text")?.toString().orEmpty()

        // Skip empty system-filler notifications (no title and no text → no signal).
        if (title.isBlank() && text.isBlank()) return

        Log.i(TAG, "Posted pkg=$pkg title=\"$title\" text=\"$text\"")

        ActivityLog.add(
            kind = ActivityLogKind.NOTIFICATION_POSTED,
            summary = title.ifBlank { pkg },
            detail = text.ifBlank { null },
        )

        val c = classifier ?: return
        scope.launch {
            val importance = try {
                c.classify(pkg, title, text)
            } catch (t: Throwable) {
                Log.w(TAG, "Classification failed for pkg=$pkg", t)
                NotificationImportance.UNKNOWN
            }
            Log.i(TAG, "Classified pkg=$pkg importance=$importance")
            ActivityLog.add(
                kind = ActivityLogKind.NOTIFICATION_CLASSIFIED,
                summary = "${title.ifBlank { pkg }} → $importance",
                detail = "pkg=$pkg",
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        Log.i(TAG, "Removed pkg=${sbn.packageName}")
    }

    override fun onDestroy() {
        scope.cancel()
        // Router ownership: don't close here — GadiOverlayService manages the engine lifecycle.
        // If listener service is destroyed but overlay is still alive, closing would break chat.
        router = null
        classifier = null
        super.onDestroy()
    }

    private companion object {
        const val TAG = "GadiNotifListener"
    }
}
