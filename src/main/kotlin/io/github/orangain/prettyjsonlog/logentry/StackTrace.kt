package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode

private val stackTraceNodeExtractors: List<NodeExtractor> = listOf(
    { it.get("stack_trace") },
    { it.get("exception") },
    { it.get("error.stack_trace") },
    { it.get("err")?.get("stack") },
    { it.get("@x") },
)

fun extractStackTrace(node: JsonNode): String? {
    return stackTraceNodeExtractors.firstNotNullOfOrNull { it(node) }?.asText()
}
