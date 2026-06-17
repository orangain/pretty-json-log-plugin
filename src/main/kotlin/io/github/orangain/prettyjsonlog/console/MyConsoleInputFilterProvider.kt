package io.github.orangain.prettyjsonlog.console

import com.intellij.execution.filters.ConsoleDependentInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.search.GlobalSearchScope
import com.fasterxml.jackson.databind.JsonNode
import io.github.orangain.prettyjsonlog.json.LogLineReassembler
import io.github.orangain.prettyjsonlog.json.ParseResult
import io.github.orangain.prettyjsonlog.json.prettyPrintJson
import io.github.orangain.prettyjsonlog.logentry.*
import io.github.orangain.prettyjsonlog.service.EphemeralStateService
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// We use ConsoleDependentInputFilterProvider instead of ConsoleInputFilterProvider because we need to access
// ConsoleView and Project in the filter.
class MyConsoleInputFilterProvider : ConsoleDependentInputFilterProvider() {
    override fun getDefaultFilters(
        consoleView: ConsoleView,
        project: Project,
        scope: GlobalSearchScope
    ): MutableList<InputFilter> {
        thisLogger().debug("getDefaultFilters")
        return mutableListOf(MyConsoleInputFilter(consoleView, project))
    }
}

private val zoneId = ZoneId.systemDefault()
private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

class MyConsoleInputFilter(
    private val consoleView: ConsoleView,
    private val project: Project
) : InputFilter {
    // A single log line longer than the console read buffer (~8 KB) is delivered here in multiple
    // fragments, so we reassemble it before parsing. See LogLineReassembler for details.
    private val reassembler = LogLineReassembler()

    override fun applyFilter(
        text: String,
        contentType: ConsoleViewContentType
    ): MutableList<Pair<String, ConsoleViewContentType>>? {
        thisLogger().debug("contentType: $contentType, applyFilter: $text")
        if (!isEnabled()) {
            // Flush any in-progress buffer so toggling formatting off never drops text.
            reassembler.drain()?.let { return mutableListOf(Pair(it + text, contentType)) }
            return null
        }

        return when (val result = reassembler.feed(text)) {
            is ParseResult.Complete -> format(result.node, result.suffixWhitespaces, contentType)
            is ParseResult.Passthrough -> mutableListOf(Pair(result.text, contentType))
            ParseResult.Buffering -> mutableListOf() // Empty list suppresses this fragment until the line completes.
            ParseResult.NotJson -> null
        }
    }

    private fun format(
        node: JsonNode,
        suffixWhitespaces: String,
        contentType: ConsoleViewContentType
    ): MutableList<Pair<String, ConsoleViewContentType>> {
        val timestamp = extractTimestamp(node)
        val level = extractLevel(node)
        val message = extractMessage(node)
        val stackTrace = extractStackTrace(node)
        // .trimEnd('\n') is necessary because of the following reasons:
        // - When stackTrace is null or empty, we don't want to add an extra newline.
        // - When stackTrace ends with a newline, trimming the last newline makes a folding marker look better.
        val coloredMessage = "$level: $message\n${stackTrace ?: ""}".trimEnd('\n')

        val jsonString = prettyPrintJson(node)
        return mutableListOf(
            Pair("[${timestamp?.format(zoneId, timestampFormatter)}] ", contentType),
            Pair(coloredMessage, contentTypeOf(level, contentType)),
            Pair(
                " \n$jsonString$suffixWhitespaces", // Adding a space at the end of line makes a folding marker look better.
                contentType
            ),
        )
    }

    private fun isEnabled(): Boolean {
        val service = project.service<EphemeralStateService>()
        return service.isEnabled(consoleView)
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
