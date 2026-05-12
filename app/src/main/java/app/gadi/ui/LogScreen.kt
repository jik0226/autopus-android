package app.gadi.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gadi.R
import app.gadi.log.ActivityLog
import app.gadi.log.ActivityLogEntry
import app.gadi.log.ActivityLogKind
import app.gadi.notification.NotificationRule
import app.gadi.notification.NotificationRulesStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main screen — live activity log timeline.
 *
 * Subscribes to [ActivityLog.entries] and renders newest-first. Notification
 * entries are long-pressable: opens a rule dialog that maps the source
 * package to one of [NotificationRule] (default / always important /
 * always normal / ignore). Rule changes take effect immediately on the
 * next notification (no service restart needed).
 *
 * Visual goal: signal "Gadi is learning from what's happening." Every row
 * corresponds to a real event so the timeline doubles as a feedback loop.
 */
@Composable
fun LogScreen() {
    val context = LocalContext.current
    val entries by ActivityLog.entries.collectAsState()
    val rulesVersion by NotificationRulesStore.version.collectAsState()

    var ruleDialogTarget by remember { mutableStateOf<String?>(null) }

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
                    val isNotification = entry.kind == ActivityLogKind.NOTIFICATION_POSTED ||
                        entry.kind == ActivityLogKind.NOTIFICATION_CLASSIFIED
                    val pkg = entry.packageName
                    val onLongPress: (() -> Unit)? = if (isNotification && pkg != null) {
                        { ruleDialogTarget = pkg }
                    } else null
                    // Recompose-on-version-bump: reading rulesVersion ensures
                    // any rule edit triggers a re-render of the badges below.
                    @Suppress("UNUSED_VARIABLE")
                    val versionTrigger = rulesVersion
                    val currentRule = pkg?.let { NotificationRulesStore.get(context, it) }
                        ?: NotificationRule.DEFAULT
                    LogEntryRow(entry, onLongPress, currentRule)
                }
            }
        }
    }

    ruleDialogTarget?.let { pkg ->
        @Suppress("UNUSED_VARIABLE")
        val versionTrigger = rulesVersion
        val currentRule = NotificationRulesStore.get(context, pkg)
        RuleDialog(
            packageName = pkg,
            currentRule = currentRule,
            onDismiss = { ruleDialogTarget = null },
            onSelect = { rule ->
                NotificationRulesStore.set(context, pkg, rule)
                ruleDialogTarget = null
            },
        )
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
                text = "알림을 길게 눌러 앱별 규칙을 설정해요",
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LogEntryRow(
    entry: ActivityLogEntry,
    onLongPress: (() -> Unit)?,
    currentRule: NotificationRule,
) {
    val baseModifier = Modifier
        .fillMaxWidth()
        .background(Color.White, RoundedCornerShape(10.dp))
    val rowModifier = if (onLongPress != null) {
        baseModifier.combinedClickable(
            onClick = {},
            onLongClick = onLongPress,
        )
    } else {
        baseModifier
    }
    Row(
        modifier = rowModifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                if (currentRule != NotificationRule.DEFAULT) {
                    Spacer(modifier = Modifier.width(8.dp))
                    RuleBadge(currentRule)
                }
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

@Composable
private fun RuleBadge(rule: NotificationRule) {
    val (label, color) = when (rule) {
        NotificationRule.ALWAYS_IMPORTANT -> "강제 중요" to Color(0xFFD97706)
        NotificationRule.ALWAYS_NORMAL -> "강제 일반" to Color(0xFF64748B)
        NotificationRule.IGNORE -> "무시" to Color(0xFFEF4444)
        NotificationRule.DEFAULT -> return
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text = label, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RuleDialog(
    packageName: String,
    currentRule: NotificationRule,
    onDismiss: () -> Unit,
    onSelect: (NotificationRule) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "앱 분류 규칙", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(text = packageName, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        },
        text = {
            Column {
                RuleOption("기본 분류", "자동으로 중요/일반 판단", NotificationRule.DEFAULT, currentRule, onSelect)
                Spacer(modifier = Modifier.height(6.dp))
                RuleOption("항상 중요", "Gadi가 즉시 알림", NotificationRule.ALWAYS_IMPORTANT, currentRule, onSelect)
                Spacer(modifier = Modifier.height(6.dp))
                RuleOption("항상 일반", "기록만, 알림 X", NotificationRule.ALWAYS_NORMAL, currentRule, onSelect)
                Spacer(modifier = Modifier.height(6.dp))
                RuleOption("무시", "기록조차 안 함", NotificationRule.IGNORE, currentRule, onSelect)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        },
    )
}

@Composable
private fun RuleOption(
    label: String,
    subtitle: String,
    rule: NotificationRule,
    currentRule: NotificationRule,
    onSelect: (NotificationRule) -> Unit,
) {
    val selected = rule == currentRule
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) Color(0xFFEAF5EF) else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onSelect(rule) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = Color(0xFF172033),
            )
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
        }
        if (selected) {
            Text(text = "✓", color = Color(0xFF1F6F4A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
