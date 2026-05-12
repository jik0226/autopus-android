package app.gadi.notification

import android.app.PendingIntent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Important-enough-to-surface notification snapshot.
 *
 * Captured at classification time so the overlay can present it even after
 * the system removes the original [android.service.notification.StatusBarNotification].
 */
data class ImportantNotification(
    val packageName: String,
    val title: String,
    val text: String,
    val timestampMillis: Long,
    val contentIntent: PendingIntent?,
)

/**
 * Process-wide event bus for IMPORTANT notifications.
 *
 * Decouples [GadiNotificationListenerService] (producer) from
 * [app.gadi.GadiOverlayService] (consumer). `replay = 0` so a fresh
 * subscriber does not see past events; `extraBufferCapacity = 8` lets a
 * short burst land without drops while the overlay redraws.
 *
 * Privacy: stays on-device. Never serialized off-device.
 */
object NotificationEvents {
    private val _important = MutableSharedFlow<ImportantNotification>(
        replay = 0,
        extraBufferCapacity = 8,
    )
    val important: SharedFlow<ImportantNotification> = _important.asSharedFlow()

    suspend fun emit(event: ImportantNotification) {
        _important.emit(event)
    }
}
