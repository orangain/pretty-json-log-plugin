package io.github.orangain.prettyjsonlog.listeners

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.execution.filters.ConsoleInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyConsoleInputFilterProvider : ConsoleInputFilterProvider {
    override fun getDefaultFilters(project: Project): Array<InputFilter> {
        return arrayOf(MyConsoleInputFilter())
    }
}

private val zoneId = ZoneId.systemDefault()
private val timestampFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
private val timestampKeys = listOf("timestamp", "time")
private val levelKeys = listOf("level", "severity")
private val messageKeys = listOf("message", "msg")

class MyConsoleInputFilter : InputFilter {
    override fun applyFilter(
        text: String,
        contentType: ConsoleViewContentType
    ): MutableList<Pair<String, ConsoleViewContentType>>? {
        thisLogger().debug("contentType: $contentType, applyFilter: $text")
        val node = parseJson(text) ?: return null

        val keys = Iterable { node.fieldNames() }.toSet()
        val timestampKey = detectKey(keys, timestampKeys)
        val levelKey = detectKey(keys, levelKeys)
        val messageKey = detectKey(keys, messageKeys)

        val timestamp = timestampKey?.let { node.get(it) }
            ?.asText()
            ?.let { OffsetDateTime.parse(it).atZoneSameInstant(zoneId) }
            ?.format(timestampFormatter)
        val level = levelKey?.let { node.get(it) }
            ?.asText()
            ?.uppercase()
            ?: "DEFAULT"
        val message = messageKey?.let { node.get(it) }
            ?.asText()

        val restKeys = (keys - setOfNotNull(timestampKey, levelKey, messageKey)).sorted()

//        return mutableListOf(
//            Pair("[$timestamp] ", contentType),
//            Pair(level, contentTypeOf(level, contentType)),
//            Pair(": ", contentType),
//            Pair(message, ConsoleViewContentType.LOG_VERBOSE_OUTPUT),
//        )
        return (listOf(
            Pair("[$timestamp] ", contentType),
            Pair("$level: $message", contentTypeOf(level, contentType)),
        ) + restKeys.map { Pair("\n    $it: ${node.get(it)}", contentType) }).toMutableList()
    }
}

private val jsonPattern = Regex("""^\s*\{.*}\s*$""")
private val mapper = jacksonObjectMapper()

private fun parseJson(text: String): JsonNode? {
    if (!jsonPattern.matches(text)) {
        return null
    }
    return try {
        mapper.readTree(text)
    } catch (e: JsonProcessingException) {
        null
    }
}

private fun detectKey(keys: Set<String>, candidates: List<String>): String? {
    return candidates.firstOrNull { keys.contains(it) }
}

private fun contentTypeOf(level: String, inputContentType: ConsoleViewContentType): ConsoleViewContentType {
    return when (level) {
        "DEBUG" -> ConsoleViewContentType.LOG_DEBUG_OUTPUT
        "INFO", "NOTICE" -> ConsoleViewContentType.LOG_INFO_OUTPUT
        "WARNING" -> ConsoleViewContentType.LOG_WARNING_OUTPUT
        "ERROR", "CRITICAL", "ALERT", "EMERGENCY" -> ConsoleViewContentType.LOG_ERROR_OUTPUT
        else -> inputContentType
    }
}