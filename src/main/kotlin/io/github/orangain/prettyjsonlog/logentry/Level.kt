package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode

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
