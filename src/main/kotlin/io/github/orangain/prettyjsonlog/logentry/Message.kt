package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode
import io.github.orangain.prettyjsonlog.AppSettings

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

fun extractMessage1(node: JsonNode): String? {
    return messageKeys.firstNotNullOfOrNull { it(node) }?.asText()
}



fun extractMessage(node: JsonNode): String? {
    var extractedMessage: String? = ""

    val messageConfig = AppSettings.getInstance().state?.messageFields
    if (!messageConfig.isNullOrEmpty()) {

        for (key in messageConfig.split(',')) {
            val keyValue = key.trim()
            var currNode = node
            var valNode:JsonNode? = null

            for (field in keyValue.split('.')) {
                if (currNode.has(field)) {
                    valNode = currNode.get(field)
                    currNode = valNode
                }
            }
            if(valNode!=null && valNode.asText().isNotEmpty()) {
                if (!extractedMessage.isNullOrEmpty() ) {
                    extractedMessage += " | "
                }
                extractedMessage += valNode.asText()
            }
        }
    }
    return  extractedMessage
}
