package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode
import io.github.orangain.prettyjsonlog.AppSettings


/**
 * Extracts a message from a JSON node based on the configured message fields in AppSettings.
 *
 * @param node The JSON node from which to extract the message.
 * @return The extracted message as a String, or null if no message could be extracted.
 */
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
                } else {
                    valNode = null
                    break
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

fun extractFieldsFromText(text: String): String? {
    var extractedMessage: String? = ""

    val messageConfig = AppSettings.getInstance().state?.messageFields
    if (!messageConfig.isNullOrEmpty()) {

        for (key in messageConfig.split(',')) {
            val keyValue = key.trim()
            var currText = text.dropLast(text.length -1200) // Limit the text to the last 1200 characters to avoid performance issues with large logs
            var valText: String? = null
            // Use a regex to find the message in the text
            try {
                for (field in keyValue.split('.')) {
                    //return  "string too large: $field"
                    if (Regex("\"$field\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)[\",\n]?").find(currText)?.value != null) {
                        valText = Regex("\"$field\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)[\",\n]?").find(currText)?.groups?.get(1)?.value
                        currText = valText ?: ""
                    } else {
                        valText = null
                        break
                    }
                }
            } catch (e: Exception) {
                // If there's an error in regex matching, skip this key
                return "$extractedMessage$key(parse error)"
            }
            if(!valText.isNullOrEmpty()) {
                if (!extractedMessage.isNullOrEmpty() ) {
                    extractedMessage += " | "
                }
                extractedMessage += valText
            }
        }
    }
    return  extractedMessage
}
