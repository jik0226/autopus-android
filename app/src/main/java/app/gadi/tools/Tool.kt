package app.gadi.tools

/**
 * Tool abstraction for on-device LLM tool-use pattern.
 *
 * Phone-state tools provide structured data (time, battery, network, etc.)
 * so the LLM does not need to know real-time values. The intent classifier
 * picks the right tool by keyword + LLM hint, executes it, and feeds the
 * result back to the LLM to phrase a natural-language reply.
 *
 * Tools must be permission-safe at this layer: anything that needs runtime
 * permission belongs to a separate v0.3 tool group.
 */
interface Tool {
    /** Stable identifier used by intent classifier and logs. */
    val name: String

    /** Short Korean description shown to the LLM via system prompt. */
    val description: String

    /** Execute the tool and return a human-readable Korean string. */
    suspend fun execute(): String
}
