package app.gadi.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * v0.2a — Notification listener skeleton.
 *
 * Receives system-wide notification events once the user grants access in
 * Settings → Notifications → Gadi (BIND_NOTIFICATION_LISTENER_SERVICE is a
 * system permission granted only via that special settings toggle, not via
 * a runtime prompt).
 *
 * Current behavior: log notifications only. v0.2b will add LLM importance
 * classification; v0.2c will surface results in the mascot speech bubble
 * and add a tap-to-open-original-app action; v0.2d will add per-package
 * allow/block list.
 *
 * Privacy invariant: all notification content stays on-device. No external
 * transmission, no persistence beyond logcat at this stage.
 */
class GadiNotificationListenerService : NotificationListenerService() {

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
        val extras = sbn.notification?.extras
        val title = extras?.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras?.getCharSequence("android.text")?.toString().orEmpty()
        Log.i(TAG, "Posted pkg=$pkg title=\"$title\" text=\"$text\"")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        Log.i(TAG, "Removed pkg=${sbn.packageName}")
    }

    private companion object {
        const val TAG = "GadiNotifListener"
    }
}
