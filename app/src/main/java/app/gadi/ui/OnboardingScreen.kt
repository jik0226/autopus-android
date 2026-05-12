package app.gadi.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gadi.R

/**
 * First-run onboarding shown until both required permissions are granted.
 *
 * Steps shown one at a time (or both at once when neither is granted):
 *  - SYSTEM_ALERT_WINDOW (floating mascot overlay)
 *  - BIND_NOTIFICATION_LISTENER_SERVICE (notification access)
 *
 * Granted steps collapse into a check row so the user sees progress
 * instead of disappearing UI. When both are granted MainActivity routes to
 * the home log screen and auto-starts the overlay service.
 */
@Composable
fun OnboardingScreen(
    needsOverlay: Boolean,
    needsNotificationListener: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestNotificationListener: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.gadi_idle),
            contentDescription = "Gadi",
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Gadi가 동작하려면",
            fontSize = 16.sp,
            color = Color(0xFF64748B),
        )
        Text(
            text = "두 가지 권한이 필요해요",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172033),
        )
        Spacer(modifier = Modifier.height(28.dp))

        PermissionStep(
            title = "화면 위 표시",
            description = "다른 앱 위에 Gadi가 떠다닐 수 있게요.",
            done = !needsOverlay,
            onClick = onRequestOverlay,
        )
        Spacer(modifier = Modifier.height(12.dp))
        PermissionStep(
            title = "알림 접근",
            description = "알림을 분류해서 중요한 것만 알려줄 수 있게요.",
            done = !needsNotificationListener,
            onClick = onRequestNotificationListener,
        )
    }
}

@Composable
private fun PermissionStep(
    title: String,
    description: String,
    done: Boolean,
    onClick: () -> Unit,
) {
    val background = if (done) Color(0xFFEAF5EF) else Color.White
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (done) {
                Text(
                    text = "✓ ",
                    color = Color(0xFF1F6F4A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (done) Color(0xFF1F6F4A) else Color(0xFF172033),
            )
        }
        if (!done) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF64748B),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onClick) {
                Text(text = "권한 켜기")
            }
        }
    }
}
