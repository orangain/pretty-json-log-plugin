package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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

private val timestampKeys = listOf("timestamp", "time", "@timestamp")

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
