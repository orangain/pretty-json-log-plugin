package io.github.orangain.prettyjsonlog.console

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.execution.filters.ConsoleInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import io.github.orangain.prettyjsonlog.Level
import io.github.orangain.prettyjsonlog.extractLevel
import io.github.orangain.prettyjsonlog.extractMessage
import io.github.orangain.prettyjsonlog.extractTimestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyConsoleInputFilterProvider : ConsoleInputFilterProvider {
    override fun getDefaultFilters(project: Project): Array<InputFilter> {
        return arrayOf(MyConsoleInputFilter())
    }
}

private val zoneId = ZoneId.systemDefault()
private val timestampFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

class MyConsoleInputFilter : InputFilter {
    override fun applyFilter(
        text: String,
        contentType: ConsoleViewContentType
    ): MutableList<Pair<String, ConsoleViewContentType>>? {
        thisLogger().debug("contentType: $contentType, applyFilter: $text")
        val node = parseJson(text) ?: return null

        val timestamp = extractTimestamp(node)
        val level = extractLevel(node)
        val message = extractMessage(node)

        val jsonString = prettyPrintJson(node)
//        return mutableListOf(
//            Pair("[$timestamp] ", contentType),
//            Pair(level, contentTypeOf(level, contentType)),
//            Pair(": ", contentType),
//            Pair(message, ConsoleViewContentType.LOG_VERBOSE_OUTPUT),
//        )
        return mutableListOf(
            Pair("[${timestamp?.format(zoneId, timestampFormatter)}] ", contentType),
            Pair("$level: $message", contentTypeOf(level, contentType)),
            Pair("\n$jsonString", contentType),
        )
    }
}

private val jsonPattern = Regex("""^\s*\{.*}\s*$""")
private val mapper = jacksonObjectMapper().apply {
    configure(SerializationFeature.INDENT_OUTPUT, true)
    configure(JsonNodeFeature.WRITE_PROPERTIES_SORTED, true)
}
private val writer = mapper.writer(MyPrettyPrinter())

fun prettyPrintJson(node: JsonNode): String {
    return writer.writeValueAsString(node)
}

class MyPrettyPrinter : DefaultPrettyPrinter() {
    init {
        _objectFieldValueSeparatorWithSpaces = ": "
    }

    override fun createInstance(): DefaultPrettyPrinter {
        return MyPrettyPrinter()
    }
}

fun parseJson(text: String): JsonNode? {
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

private fun contentTypeOf(level: Level?, inputContentType: ConsoleViewContentType): ConsoleViewContentType {
    return when (level) {
        Level.TRACE, Level.DEBUG -> ConsoleViewContentType.LOG_DEBUG_OUTPUT
        Level.INFO -> ConsoleViewContentType.LOG_INFO_OUTPUT
        Level.WARN -> ConsoleViewContentType.LOG_WARNING_OUTPUT
        Level.ERROR, Level.FATAL -> ConsoleViewContentType.LOG_ERROR_OUTPUT
        else -> inputContentType
    }
}