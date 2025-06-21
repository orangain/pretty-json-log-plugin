package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode

private val messageKeys = listOf("message", "msg", "error.message", "@m", "RenderedMessage", "logMessage")

fun extractMessage(node: JsonNode): String? {
    return messageKeys.firstNotNullOfOrNull { node.get(it) }?.asText()
}
