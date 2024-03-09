package io.github.orangain.prettyjsonlog

import com.fasterxml.jackson.databind.JsonNode
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val timestampKeys = listOf("timestamp", "time")

fun extractTimestamp(node: JsonNode, zoneId: ZoneId, formatter: DateTimeFormatter): String? {
    return timestampKeys.firstOrNull { node.has(it) }?.let { timestampKey ->
        node.get(timestampKey)
            ?.asText()
            ?.let {
                try {
                    OffsetDateTime.parse(it).atZoneSameInstant(zoneId).format(formatter)
                } catch (e: DateTimeParseException) {
                    it
                }
            }
    }
}

private val levelKeys = listOf("level", "severity")

fun extractLevel(node: JsonNode): String? {
    return levelKeys.firstOrNull { node.has(it) }?.let { levelKey ->
        node.get(levelKey)
            ?.asText()
            ?.uppercase()
    }
}

private val messageKeys = listOf("message", "msg")

fun extractMessage(node: JsonNode): String? {
    return messageKeys.firstOrNull { node.has(it) }?.let { messageKey ->
        node.get(messageKey)
            ?.asText()
    }
}
