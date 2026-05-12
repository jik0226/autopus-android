package app.gadi.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gadi.R
import app.gadi.log.ActivityLog
import app.gadi.log.ActivityLogEntry
import app.gadi.log.ActivityLogKind
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main screen — live activity log timeline.
 *
 * Replaces the static placeholder home screen. Subscribes to
 * [ActivityLog.entries] and renders newest-first. Empty state nudges the
 * user to chat or wait for a notification so they see something appear.
 *
 * Visual goal: signal "Gadi is learning from what's happening." Every
 * entry corresponds to a real event (chat turn or notification posted/
 * classified), so the timeline doubles as a feedback loop for the user.
 */
@Composable
fun LogScreen() {
    val entries by ActivityLog.entries.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        LearningHeader()
        Spacer(modifier = Modifier.height(12.dp))

        if (entries.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(entries) { entry ->
                    LogEntryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun LearningHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.gadi_idle),
            contentDescription = "Gadi",
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Gadi가 학습 중이에요",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF172033),
            )
            Text(
                text = "알림과 대화를 보고 패턴을 익혀요",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "아직 기록이 없어요",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gadi와 채팅하거나 알림이 오면 여기에 쌓여요",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
            )
        }
    }
}

@Composable
private fun LogEntryRow(entry: ActivityLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = entry.kindGlyph(),
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 10.dp, top = 1.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.kindLabel(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = entry.kindColor(),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTime(entry.timestampMillis),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.summary,
                fontSize = 14.sp,
                color = Color(0xFF172033),
            )
            entry.detail?.let { detail ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detail,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                )
            }
        }
    }
}

private fun ActivityLogEntry.kindGlyph(): String = when (kind) {
    ActivityLogKind.CHAT_USER -> "🗨"
    ActivityLogKind.CHAT_GADI -> "🐶"
    ActivityLogKind.NOTIFICATION_POSTED -> "🔔"
    ActivityLogKind.NOTIFICATION_CLASSIFIED -> "🏷"
    ActivityLogKind.TOOL_USED -> "🛠"
}

private fun ActivityLogEntry.kindLabel(): String = when (kind) {
    ActivityLogKind.CHAT_USER -> "나"
    ActivityLogKind.CHAT_GADI -> "Gadi"
    ActivityLogKind.NOTIFICATION_POSTED -> "알림"
    ActivityLogKind.NOTIFICATION_CLASSIFIED -> "분류"
    ActivityLogKind.TOOL_USED -> "도구"
}

private fun ActivityLogEntry.kindColor(): Color = when (kind) {
    ActivityLogKind.CHAT_USER -> Color(0xFF2563EB)
    ActivityLogKind.CHAT_GADI -> Color(0xFFD97706)
    ActivityLogKind.NOTIFICATION_POSTED -> Color(0xFF6366F1)
    ActivityLogKind.NOTIFICATION_CLASSIFIED -> Color(0xFF1F6F4A)
    ActivityLogKind.TOOL_USED -> Color(0xFF8B5CF6)
}

private val TIME_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.KOREA)

private fun formatTime(millis: Long): String = TIME_FORMAT.format(Date(millis))
