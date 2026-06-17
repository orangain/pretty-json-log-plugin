package io.github.orangain.prettyjsonlog.json

import com.fasterxml.jackson.databind.JsonNode

/**
 * The outcome of feeding one console fragment to [LogLineReassembler].
 */
sealed interface ParseResult {
    /** A complete JSON log line was parsed and should be formatted. */
    data class Complete(val node: JsonNode, val suffixWhitespaces: String) : ParseResult

    /** Buffering was given up (not JSON, or too large); emit this accumulated text verbatim. */
    data class Passthrough(val text: String) : ParseResult

    /** The fragment is part of a not-yet-complete JSON line; it should be suppressed for now. */
    data object Buffering : ParseResult

    /** The fragment is not JSON and was not buffered; leave it unchanged. */
    data object NotJson : ParseResult
}

// IntelliJ's com.intellij.util.io.BaseOutputReader reads process output into a char[8192] buffer
// and, with the default Options (splitToLines=true, sendIncompleteLines=true, withSeparators=true),
// flushes an incomplete line after each read even when no newline has been seen yet. As a result, a
// single log line longer than ~8192 chars is delivered to the console InputFilter in multiple
// fragments. Only engage buffering for a fragment that plausibly is such a buffer-full split: it
// does not terminate the line (no trailing newline), it is about as large as the platform read
// buffer, and it starts like a JSON object.
private const val SPLIT_THRESHOLD = 8000

// Safety cap so that pathological non-JSON output starting with '{' and lacking newlines cannot grow
// the buffer without bound.
private const val MAX_BUFFER = 8 * 1024 * 1024 // 8 MiB

/**
 * Reassembles a single JSON log line that the IntelliJ console delivered as multiple fragments.
 *
 * One instance is held per console. Console output is flushed on a single thread per console, so the
 * mutable [buffer] does not need synchronization.
 */
class LogLineReassembler {
    private val buffer = StringBuilder()

    fun feed(text: String): ParseResult {
        if (buffer.isEmpty()) {
            // Fast path: the common case where a whole line arrives in one fragment.
            parseJson(text)?.let { (node, suffix) -> return ParseResult.Complete(node, suffix) }
            return if (looksLikeSplitJsonStart(text)) {
                buffer.append(text)
                ParseResult.Buffering
            } else {
                ParseResult.NotJson
            }
        }

        // Buffering in progress: append and try to complete the line.
        buffer.append(text)
        parseJson(buffer.toString())?.let { (node, suffix) ->
            buffer.setLength(0)
            return ParseResult.Complete(node, suffix)
        }
        if (text.endsWith("\n") || buffer.length > MAX_BUFFER) {
            // The line ended (or overflowed the cap) but is not valid JSON. Give up and emit the
            // accumulated text verbatim so that nothing previously suppressed is lost.
            return ParseResult.Passthrough(drainBuffer())
        }
        return ParseResult.Buffering
    }

    /**
     * Returns and clears any partially-buffered text, or null if nothing is buffered. Used to avoid
     * losing suppressed fragments when formatting is toggled off mid-line.
     */
    fun drain(): String? = if (buffer.isEmpty()) null else drainBuffer()

    private fun drainBuffer(): String {
        val raw = buffer.toString()
        buffer.setLength(0)
        return raw
    }
}

private fun looksLikeSplitJsonStart(text: String): Boolean {
    return !text.endsWith("\n") && text.length >= SPLIT_THRESHOLD && text.trimStart().startsWith("{")
}
