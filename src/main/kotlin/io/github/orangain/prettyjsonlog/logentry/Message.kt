package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode

//private val messageKeys = listOf("message", "msg", "error.message", "@m", "RenderedMessage")

private val messageKeys:List<NodeExtractor> = listOf(
    { it.get("message") },
    { it.get("msg") },
    { it.get("sMsg.msg") },
    { it.get("sMsg")?.get("msg") },
    { it.get("error.message") },
    { it.get("@m") },
    { it.get("RenderedMessage") },
    { it.get("msgType") },
)

fun extractMessage(node: JsonNode): String? {
    return messageKeys.firstNotNullOfOrNull { it(node) }?.asText()
}
