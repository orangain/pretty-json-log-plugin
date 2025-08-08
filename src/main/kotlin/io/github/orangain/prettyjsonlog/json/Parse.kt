package io.github.orangain.prettyjsonlog.json

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

private val jsonPattern = Regex("""^\s*(\{.*})(\s*)$""")

fun parseJson(text: String): Pair<JsonNode, String>? {

     try {
        val result = jsonPattern.matchEntire(text) ?: return null

        return Pair(mapper.readTree(result.groups[1]!!.value), result.groups[2]!!.value)
    } catch (e: Exception) {
        return null
    }
}

fun getJson(jsonText: String): Pair<JsonNode, String>? {
    return try {
        val trimmedText = jsonText.trim()
        val node = mapper.readTree(trimmedText)
        if (jsonText.length > (8 * 1024)) {
            // If the JSON is too large, remove heavy properties to avoid performance issues
            removeHeavyProperties(node)
        }
        val trailingSpaces = jsonText.substring(trimmedText.length)
        Pair(node, trailingSpaces)
    } catch (e: JsonProcessingException) {
        null
    }
}

private fun removeHeavyProperties(node: JsonNode): JsonNode {
    if (node is ObjectNode) {
        // Remove properties that are not needed for pretty printing
        node.remove("message")
    }
    return node
}
