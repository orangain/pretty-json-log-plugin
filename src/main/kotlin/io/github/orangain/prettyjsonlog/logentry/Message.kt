package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode

private val messageKeys = listOf("message", "msg", "error.message", "@m", "RenderedMessage", "logMessage")
private val messageExtractors = buildNodeExtractors(messageKeys)

fun extractMessage(node: JsonNode): String? {
    return messageExtractors.firstNotNullOfOrNull { it(node) }?.asText()
}
