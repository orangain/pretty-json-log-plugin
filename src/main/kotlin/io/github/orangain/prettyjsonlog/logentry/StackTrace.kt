package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode
import io.github.orangain.prettyjsonlog.AppSettings

/**
 * Extracts a stack trace from a JSON node based on the configured error fields in AppSettings.
 *
 * @param node The JSON node from which to extract the stack trace.
 * @return The extracted stack trace as a String, or null if no stack trace could be extracted.
 */
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
