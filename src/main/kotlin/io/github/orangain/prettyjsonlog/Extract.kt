package io.github.orangain.prettyjsonlog

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val timestampKeys = listOf("timestamp", "time")

sealed interface Timestamp {
    fun format(zoneId: ZoneId, formatter: DateTimeFormatter): String
    data class Parsed(val value: Instant) : Timestamp {
        override fun format(zoneId: ZoneId, formatter: DateTimeFormatter): String {
            return value.atZone(zoneId).format(formatter)
        }
    }

    data class Fallback(val value: String) : Timestamp {
        override fun format(zoneId: ZoneId, formatter: DateTimeFormatter): String {
            return value
        }
    }
}

fun extractTimestamp(node: JsonNode): Timestamp? {

    return timestampKeys.firstOrNull { node.has(it) }?.let { timestampKey ->
        node.get(timestampKey)
            ?.let { node ->
                if (node.isNumber) {
                    // We assume that the number is a Unix timestamp in milliseconds.
                    Timestamp.Parsed(Instant.ofEpochMilli(node.asLong()))
                } else {
                    val text = node.asText()
                    try {
                        // Use OffsetDateTime.parse instead of Instant.parse because Instant.parse in JDK <= 11 does not support non-UTC offset like "-05:00".
                        // See: https://stackoverflow.com/questions/68217689/how-to-use-instant-java-class-to-parse-a-date-time-with-offset-from-utc/68221614#68221614
                        Timestamp.Parsed(OffsetDateTime.parse(text).toInstant())
                    } catch (e: DateTimeParseException) {
                        Timestamp.Fallback(text)
                    }
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
