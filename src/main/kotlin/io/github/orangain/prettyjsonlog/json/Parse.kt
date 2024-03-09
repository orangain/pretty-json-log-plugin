package io.github.orangain.prettyjsonlog.json

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode

private val jsonPattern = Regex("""^\s*(\{.*})(\s*)$""")

fun parseJson(text: String): Pair<JsonNode, String>? {
    val result = jsonPattern.matchEntire(text) ?: return null

    return try {
        Pair(mapper.readTree(result.groups[1]!!.value), result.groups[2]!!.value)
    } catch (e: JsonProcessingException) {
        null
    }
}