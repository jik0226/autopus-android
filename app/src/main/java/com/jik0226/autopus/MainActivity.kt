package com.jik0226.autopus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutopusApp()
        }
    }
}

@Composable
private fun AutopusApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC),
        ) {
            HomeScreen()
        }
    }
}

@Composable
private fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StatusPill()
        Spacer(modifier = Modifier.height(24.dp))
        AutopusMascot(modifier = Modifier.size(180.dp))
        Spacer(modifier = Modifier.height(28.dp))
        SpeechBubble(text = "Autopus")
        Spacer(modifier = Modifier.height(18.dp))
        ChatBar()
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
private fun AutopusMascot(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val body = Color(0xFF7C8CF8)
        val shadow = Color(0xFF4E5ECC)
        val ink = Color(0xFF1B2141)
        val blush = Color(0xFFFFA6B7)

        drawOval(
            color = body,
            topLeft = Offset(size.width * 0.18f, size.height * 0.08f),
            size = Size(size.width * 0.64f, size.height * 0.58f),
        )

        drawOval(
            color = shadow,
            topLeft = Offset(size.width * 0.28f, size.height * 0.5f),
            size = Size(size.width * 0.44f, size.height * 0.2f),
        )

        val tentacles = listOf(0.18f, 0.32f, 0.46f, 0.6f, 0.74f)
        tentacles.forEachIndexed { index, x ->
            val curve = Path().apply {
                moveTo(size.width * x, size.height * 0.58f)
                cubicTo(
                    size.width * (x - 0.06f),
                    size.height * 0.78f,
                    size.width * (x + 0.08f),
                    size.height * 0.8f,
                    size.width * (x + 0.02f),
                    size.height * 0.93f,
                )
            }
            drawPath(
                path = curve,
                color = if (index % 2 == 0) body else shadow,
                style = Stroke(width = size.width * 0.075f),
            )
        }

        drawCircle(ink, radius = size.width * 0.035f, center = Offset(size.width * 0.41f, size.height * 0.34f))
        drawCircle(ink, radius = size.width * 0.035f, center = Offset(size.width * 0.59f, size.height * 0.34f))
        drawCircle(blush, radius = size.width * 0.035f, center = Offset(size.width * 0.31f, size.height * 0.43f))
        drawCircle(blush, radius = size.width * 0.035f, center = Offset(size.width * 0.69f, size.height * 0.43f))
    }
}

@Preview(showBackground = true)
@Composable
private fun AutopusAppPreview() {
    AutopusApp()
}
