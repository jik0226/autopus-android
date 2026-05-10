package app.gadi

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private var canDrawOverlays by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshOverlayPermission()
        setContent {
            GadiApp(
                canDrawOverlays = canDrawOverlays,
                onRequestOverlayPermission = ::openOverlayPermissionSettings,
                onStartOverlay = ::startGadiOverlay,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshOverlayPermission()
    }

    private fun refreshOverlayPermission() {
        canDrawOverlays = Settings.canDrawOverlays(this)
    }

    private fun openOverlayPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        )
        startActivity(intent)
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

@Composable
private fun GadiApp(
    canDrawOverlays: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onStartOverlay: () -> Unit,
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC),
        ) {
            if (canDrawOverlays) {
                HomeScreen(onStartOverlay = onStartOverlay)
            } else {
                OverlayPermissionScreen(onRequestOverlayPermission = onRequestOverlayPermission)
            }
        }
    }
}

@Composable
private fun OverlayPermissionScreen(onRequestOverlayPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GadiMascot(modifier = Modifier.size(180.dp))
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Gadi가 다른 앱 위에서 안전하게 도와주려면 표시 권한이 필요해요.",
            color = Color(0xFF172033),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "권한을 끄면 floating companion 기능은 사용할 수 없고, 이 화면에서 다시 안내할게요.",
            color = Color(0xFF64748B),
            fontSize = 15.sp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestOverlayPermission) {
            Text(text = "권한 허용")
        }
    }
}

@Composable
private fun HomeScreen(onStartOverlay: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StatusPill()
        Spacer(modifier = Modifier.height(24.dp))
        GadiMascot(modifier = Modifier.size(180.dp))
        Spacer(modifier = Modifier.height(28.dp))
        SpeechBubble(text = "Gadi")
        Spacer(modifier = Modifier.height(18.dp))
        ChatBar()
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onStartOverlay) {
            Text(text = "Gadi 띄우기")
        }
    }
}

@Composable
private fun StatusPill() {
    Row(
        modifier = Modifier
            .background(Color(0xFFEAF5EF), CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFF35B779), CircleShape),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "idle",
            color = Color(0xFF1F6F4A),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SpeechBubble(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Text(
            text = text,
            color = Color(0xFF172033),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ChatBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = "지금 몇 시야?",
            color = Color(0xFF64748B),
            fontSize = 16.sp,
        )
        Text(
            text = "Send",
            color = Color(0xFF2563EB),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun GadiMascot(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.gadi_idle),
        contentDescription = "Gadi idle mascot",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Preview(showBackground = true)
@Composable
private fun GadiAppPreview() {
    GadiApp(
        canDrawOverlays = true,
        onRequestOverlayPermission = {},
        onStartOverlay = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun OverlayPermissionScreenPreview() {
    GadiApp(
        canDrawOverlays = false,
        onRequestOverlayPermission = {},
        onStartOverlay = {},
    )
}
