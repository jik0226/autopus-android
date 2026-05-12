package app.gadi.llm

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class GemmaInferenceEngine(
    private val appContext: Context,
    private val modelLocator: GemmaModelLocator = GemmaModelLocator(appContext),
) : AutoCloseable {
    private var inference: LlmInference? = null

    @Synchronized
    fun generate(prompt: String): String {
        val startedAt = System.currentTimeMillis()
        val response = getOrCreateInference().generateResponse(prompt)
        Log.i(TAG, "Gemma generated ${response.length} chars in ${System.currentTimeMillis() - startedAt}ms")
        return response
    }

    @Synchronized
    fun runSmokeTest(prompt: String = SMOKE_TEST_PROMPT): String {
        val response = generate(prompt)
        Log.i(TAG, "Smoke test response: $response")
        return response
    }

    @Synchronized
    override fun close() {
        inference?.close()
        inference = null
    }

    private fun getOrCreateInference(): LlmInference {
        inference?.let { return it }

        val modelFile = modelLocator.requireModelFile()
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile.absolutePath)
            .setMaxTokens(GEMMA_CONTEXT_TOKENS)
            .setTopK(40)
            .setTemperature(0.7f)
            .build()

        return LlmInference.createFromOptions(appContext, options).also {
            inference = it
            Log.i(TAG, "Gemma model loaded from ${modelFile.absolutePath}")
        }
    }

    private companion object {
        const val GEMMA_CONTEXT_TOKENS = 1280
        const val SMOKE_TEST_PROMPT = "너는 귀여운 모바일 비서 Gadi야. 한국어로 짧게 인사해줘."
        const val TAG = "GadiGemma"
    }
}

class GemmaModelLocator(
    context: Context,
) {
    private val appContext = context.applicationContext

    fun requireModelFile(): File {
        return candidateFiles().firstOrNull { it.isFile }
            ?: error(missingModelMessage())
    }

    fun preferredInstallFile(): File = File(appContext.filesDir, INTERNAL_MODEL_PATH)

    fun candidateFiles(): List<File> = listOf(
        preferredInstallFile(),
        File(DEV_MODEL_PATH),
    )

    private fun missingModelMessage(): String {
        val paths = candidateFiles().joinToString(separator = "\n") { "- ${it.absolutePath}" }
        return "Gemma 3 1B model file is missing. Install one of:\n$paths"
    }

    companion object {
        const val MODEL_FILE_NAME = "gemma-3-1b-it.task"
        const val INTERNAL_MODEL_PATH = "models/$MODEL_FILE_NAME"
        const val DEV_MODEL_PATH = "/data/local/tmp/llm/$MODEL_FILE_NAME"
    }
}
