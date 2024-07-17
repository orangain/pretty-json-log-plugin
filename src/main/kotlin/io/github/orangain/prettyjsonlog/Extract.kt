package io.github.orangain.prettyjsonlog

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val timestampKeys = listOf("timestamp", "time", "@timestamp")

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

    companion object {
        fun fromEpochMilli(value: Long): Parsed {
            return Parsed(Instant.ofEpochMilli(value))
        }

        fun fromString(value: String): Timestamp {
            return try {
                // Use OffsetDateTime.parse instead of Instant.parse because Instant.parse in JDK <= 11 does not support non-UTC offset like "-05:00".
                // See: https://stackoverflow.com/questions/68217689/how-to-use-instant-java-class-to-parse-a-date-time-with-offset-from-utc/68221614#68221614
                Parsed(OffsetDateTime.parse(value).toInstant())
            } catch (e: DateTimeParseException) {
                Fallback(value)
            }
        }
    }
}

enum class Level {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL;

    companion object {
        fun fromInt(level: Int): Level {
            // Use bunyan's level as a reference.
            // See: https://github.com/trentm/node-bunyan?tab=readme-ov-file#levels
            return when {
                level < 20 -> TRACE
                level < 30 -> DEBUG
                level < 40 -> INFO
                level < 50 -> WARN
                level < 60 -> ERROR
                else -> FATAL
            }
        }

        fun fromString(level: String): Level? {
            // Bunyan's levels: TRACE, DEBUG, INFO, WARN, ERROR, FATAL
            // https://github.com/trentm/node-bunyan?tab=readme-ov-file#levels
            // Cloud Logging's levels: DEFAULT, DEBUG, INFO, NOTICE, WARNING, ERROR, CRITICAL, ALERT, EMERGENCY
            // https://cloud.google.com/logging/docs/reference/v2/rest/v2/LogEntry#LogSeverity
            // java.util.logging's levels: FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE
            // https://docs.oracle.com/en/java/javase/21/docs/api/java.logging/java/util/logging/Level.html
            return when (level.uppercase()) {
                "TRACE", "FINEST", "FINER", "FINE" -> TRACE
                "DEBUG", "CONFIG" -> DEBUG
                "INFO", "NOTICE" -> INFO
                "WARN", "WARNING" -> WARN
                "ERROR", "CRITICAL", "SEVERE" -> ERROR
                "FATAL", "ALERT", "EMERGENCY" -> FATAL
                else -> null // This includes "DEFAULT"
            }
        }
    }
}

fun extractTimestamp(node: JsonNode): Timestamp? {

    return timestampKeys.firstNotNullOfOrNull { node.get(it) }?.let { node ->
        if (node.isNumber) {
            // We assume that the number is a Unix timestamp in milliseconds.
            Timestamp.fromEpochMilli(node.asLong())
        } else {
            Timestamp.fromString(node.asText())
        }
    }
}

private val levelKeys = listOf("level", "severity", "log.level")

fun extractLevel(node: JsonNode): Level? {
    return levelKeys.firstNotNullOfOrNull { node.get(it) }?.let { node ->
        if (node.isNumber) {
            Level.fromInt(node.asInt())
        } else {
            Level.fromString(node.asText())
        }
    }
}

private val messageKeys = listOf("message", "msg", "error.message")

fun extractMessage(node: JsonNode): String? {
    return messageKeys.firstNotNullOfOrNull { node.get(it) }?.asText()
}

typealias NodeExtractor = (JsonNode) -> JsonNode?

private val stackTraceNodeExtractors: List<NodeExtractor> = listOf(
    { it.get("stack_trace") },
    { it.get("exception") },
    { it.get("error.stack_trace") },
    { it.get("err")?.get("stack") },
)

fun extractStackTrace(node: JsonNode): String? {
    return stackTraceNodeExtractors.firstNotNullOfOrNull { it(node) }?.asText()
}
