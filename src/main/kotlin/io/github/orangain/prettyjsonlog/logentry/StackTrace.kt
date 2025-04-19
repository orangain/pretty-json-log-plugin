package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode
import io.github.orangain.prettyjsonlog.AppSettings

private val stackTraceNodeExtractors: List<NodeExtractor> = listOf(
    { it.get("stack_trace") },
    { it.get("exception") },
    { it.get("error.stack_trace") },
    { it.get("err")?.get("stack") },
    { it.get("@x") },
    { it.get("Exception") },
)

fun extractStackTrace1(node: JsonNode): String? {
    return stackTraceNodeExtractors.firstNotNullOfOrNull { it(node) }?.asText()
}


fun extractStackTrace(node: JsonNode): String? {
    var extractedMessage: String? = ""

    val messageConfig = AppSettings.getInstance().state?.errorFields
    if (!messageConfig.isNullOrEmpty()) {

        for (key in messageConfig.split(',')) {
            val keyValue = key.trim()
            var currNode = node
            var valNode:JsonNode? = null

            for (field in keyValue.split('.')) {
                if (currNode.has(field)) {
                    val value = currNode.get(field)
                    valNode = value
                }
            }
            if(valNode!=null) {
                if (!extractedMessage.isNullOrEmpty())
                    extractedMessage += " \n "
                extractedMessage += valNode.asText()
                valNode = null
            }
        }
    }
    return  extractedMessage
}
