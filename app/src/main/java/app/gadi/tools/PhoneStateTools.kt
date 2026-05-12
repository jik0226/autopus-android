package app.gadi.tools

import android.content.Context
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * Phone-state tools — no runtime permissions required.
 *
 * All return Korean strings ready for direct LLM consumption; the LLM is
 * still responsible for phrasing them into the final reply.
 */

class GetCurrentTimeTool : Tool {
    override val name: String = "getCurrentTime"
    override val description: String = "현재 시각과 요일을 반환합니다."

    override suspend fun execute(): String {
        val now = LocalDateTime.now()
        val dayOfWeek = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
        return "${now.hour}시 ${now.minute}분 ($dayOfWeek)"
    }
}

class GetBatteryStatusTool(private val context: Context) : Tool {
    override val name: String = "getBatteryStatus"
    override val description: String = "현재 배터리 잔량과 충전 상태."

    override suspend fun execute(): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val charging = bm.isCharging
        return "${level}% (${if (charging) "충전 중" else "충전 아님"})"
    }
}

class GetNetworkStatusTool(private val context: Context) : Tool {
    override val name: String = "getNetworkStatus"
    override val description: String = "현재 네트워크 연결 상태 (Wi-Fi / 모바일 데이터 / 오프라인)."

    override suspend fun execute(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "오프라인"
        val caps = cm.getNetworkCapabilities(network) ?: return "오프라인"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi 연결됨"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "모바일 데이터 연결됨"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "이더넷 연결됨"
            else -> "기타 연결"
        }
    }
}

class GetDeviceInfoTool : Tool {
    override val name: String = "getDeviceInfo"
    override val description: String = "디바이스 제조사, 모델, Android OS 버전."

    override suspend fun execute(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val release = Build.VERSION.RELEASE
        val sdk = Build.VERSION.SDK_INT
        return "$manufacturer $model, Android $release (SDK $sdk)"
    }
}

class GetVolumeLevelTool(private val context: Context) : Tool {
    override val name: String = "getVolumeLevel"
    override val description: String = "현재 미디어 음량 (퍼센트)."

    override suspend fun execute(): String {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val percent = if (max > 0) (current * 100) / max else 0
        return "미디어 음량 ${percent}% (${current}/${max})"
    }
}

/**
 * Convenience factory — builds the full default tool list for a given Context.
 */
fun defaultPhoneStateTools(context: Context): List<Tool> = listOf(
    GetCurrentTimeTool(),
    GetBatteryStatusTool(context),
    GetNetworkStatusTool(context),
    GetDeviceInfoTool(),
    GetVolumeLevelTool(context),
)
