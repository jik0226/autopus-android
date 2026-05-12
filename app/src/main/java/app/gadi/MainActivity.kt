package app.gadi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import app.gadi.ui.LogScreen
import app.gadi.ui.OnboardingScreen

class MainActivity : ComponentActivity() {
    private var canDrawOverlays by mutableStateOf(false)
    private var notificationListenerEnabled by mutableStateOf(false)
    private var canAccessCalendar by mutableStateOf(false)

    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> refreshPermissions() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshPermissions()
        setContent {
            GadiApp(
                needsOverlay = !canDrawOverlays,
                needsNotificationListener = !notificationListenerEnabled,
                needsCalendar = !canAccessCalendar,
                onRequestOverlay = ::openOverlayPermissionSettings,
                onRequestNotificationListener = ::openNotificationListenerSettings,
                onRequestCalendar = ::requestCalendarPermissions,
                onStartOverlay = ::startGadiOverlay,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
    }

    private fun refreshPermissions() {
        canDrawOverlays = Settings.canDrawOverlays(this)
        notificationListenerEnabled = isNotificationListenerEnabled(this)
        canAccessCalendar = hasCalendarPermissions(this)
    }

    private fun openOverlayPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        )
        startActivity(intent)
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    private fun requestCalendarPermissions() {
        calendarPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR,
            )
        )
    }

    private fun startGadiOverlay() {
        val intent = Intent(this, GadiOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

private fun hasCalendarPermissions(context: Context): Boolean {
    val read = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CALENDAR,
    ) == PackageManager.PERMISSION_GRANTED
    val write = ContextCompat.checkSelfPermission(
        context, Manifest.permission.WRITE_CALENDAR,
    ) == PackageManager.PERMISSION_GRANTED
    return read && write
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
    ) ?: return false
    return flat.split(":").any { it.startsWith(context.packageName) }
}

@Composable
private fun GadiApp(
    needsOverlay: Boolean,
    needsNotificationListener: Boolean,
    needsCalendar: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestNotificationListener: () -> Unit,
    onRequestCalendar: () -> Unit,
    onStartOverlay: () -> Unit,
) {
    val allGranted = !needsOverlay && !needsNotificationListener && !needsCalendar
    // Auto-start overlay service once all permissions land.
    // Service.onStartCommand is idempotent (existing overlay view is not recreated).
    LaunchedEffect(allGranted) {
        if (allGranted) {
            onStartOverlay()
        }
    }
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF1F5F9),
        ) {
            if (allGranted) {
                LogScreen()
            } else {
                OnboardingScreen(
                    needsOverlay = needsOverlay,
                    needsNotificationListener = needsNotificationListener,
                    needsCalendar = needsCalendar,
                    onRequestOverlay = onRequestOverlay,
                    onRequestNotificationListener = onRequestNotificationListener,
                    onRequestCalendar = onRequestCalendar,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GadiAppPreview() {
    GadiApp(
        needsOverlay = false,
        needsNotificationListener = false,
        needsCalendar = false,
        onRequestOverlay = {},
        onRequestNotificationListener = {},
        onRequestCalendar = {},
        onStartOverlay = {},
    )
}
