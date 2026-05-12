package app.gadi.llm

import app.gadi.tools.Tool

/**
 * Keyword-based intent classifier for phone-state tools.
 *
 * Returns the matching Tool by inspecting the user input for Korean (primary)
 * and English (fallback) keywords. Returns null when the input is free-form
 * chat so [ToolRouter] passes it through to the base LLM chat path.
 *
 * Intentionally simple for v0.1 Gemma 3 1B; v0.3 will move intent detection
 * into the LLM with a proper tool-call schema once a larger model or
 * fine-tuned variant is in place.
 */
class IntentClassifier(private val tools: List<Tool>) {

    fun classify(userInput: String): Tool? {
        val input = userInput.lowercase()
        return when {
            matchesAny(input, TIME_KEYWORDS) -> findTool("getCurrentTime")
            matchesAny(input, BATTERY_KEYWORDS) -> findTool("getBatteryStatus")
            matchesAny(input, NETWORK_KEYWORDS) -> findTool("getNetworkStatus")
            matchesAny(input, DEVICE_KEYWORDS) -> findTool("getDeviceInfo")
            matchesAny(input, VOLUME_KEYWORDS) -> findTool("getVolumeLevel")
            else -> null
        }
    }

    private fun findTool(name: String): Tool? = tools.find { it.name == name }

    private fun matchesAny(input: String, keywords: List<String>): Boolean =
        keywords.any { it in input }

    private companion object {
        // "지금" was too greedy ("지금 배터리 얼마야?" → time match). Removed.
        val TIME_KEYWORDS = listOf("시간", "몇 시", "몇시", "시각", "time", "what time", "clock")
        val BATTERY_KEYWORDS = listOf("배터리", "잔량", "충전", "전원", "battery", "퍼센트")
        val NETWORK_KEYWORDS = listOf("와이파이", "wifi", "네트워크", "인터넷", "데이터", "연결", "lte", "5g", "온라인", "오프라인")
        val DEVICE_KEYWORDS = listOf("기종", "모델", "디바이스", "기기", "안드로이드", "버전", "device", "폰", "휴대폰", "스마트폰", "갤럭시")
        val VOLUME_KEYWORDS = listOf("음량", "볼륨", "소리", "사운드", "volume")
    }
}
