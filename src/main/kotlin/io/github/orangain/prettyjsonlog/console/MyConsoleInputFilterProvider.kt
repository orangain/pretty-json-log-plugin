package io.github.orangain.prettyjsonlog.console

import com.fasterxml.jackson.databind.JsonNode
import com.intellij.execution.filters.ConsoleInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import io.github.orangain.prettyjsonlog.json.parseJson
import io.github.orangain.prettyjsonlog.json.prettyPrintJson
import io.github.orangain.prettyjsonlog.logentry.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyConsoleInputFilterProvider : ConsoleInputFilterProvider {
    override fun getDefaultFilters(project: Project): Array<InputFilter> {
        return arrayOf(MyConsoleInputFilter())
    }
}

private val zoneId = ZoneId.systemDefault()
private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

class MyConsoleInputFilter : InputFilter {
    override fun applyFilter(
        text: String,
        contentType: ConsoleViewContentType
    ): MutableList<Pair<String, ConsoleViewContentType>>? {
        thisLogger().debug("contentType: $contentType, applyFilter: $text")
        val (node, suffixWhitespaces) = parseJson(text) ?: return null

        val timestamp = extractTimestamp(node)
        val level = extractLevel(node)
        val contentTypeOfLevel = contentTypeOf(level, contentType)
        val message = extractMessage(node)
        val stackTracePair = extractStackTracePair(node, contentTypeOfLevel)

        val jsonString = prettyPrintJson(node)
        return mutableListOf(
            Pair("[${timestamp?.format(zoneId, timestampFormatter)}] ", contentType),
            Pair("$level: $message", contentTypeOfLevel),
            stackTracePair,
            Pair(
                " \n$jsonString$suffixWhitespaces",
                contentType
            ), // Add a space to at the end of line to make it look good when folded.
        )
    }

    private fun extractStackTracePair(
        node: JsonNode,
        contentTypeOfLevel: ConsoleViewContentType
    ): Pair<String, ConsoleViewContentType> {
        val stackTrace = extractStackTrace(node)

        if (stackTrace?.isNotEmpty() == true) {
            return Pair("\n$stackTrace", contentTypeOfLevel)
        }
        return Pair("", contentTypeOfLevel)
    }
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
