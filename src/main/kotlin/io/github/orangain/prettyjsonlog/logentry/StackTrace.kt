package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode

private val stackTraceKeys = listOf(
    "stack_trace",
    "exception",
    "error.stack_trace",
    "err.stack",
    "@x",
    "Exception"
)
private val stackTraceNodeExtractors = buildNodeExtractors(stackTraceKeys)

fun extractStackTrace(node: JsonNode): String? {
    return stackTraceNodeExtractors.firstNotNullOfOrNull { it(node) }?.asText()
}
