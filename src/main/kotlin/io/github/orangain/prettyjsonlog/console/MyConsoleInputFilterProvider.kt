package io.github.orangain.prettyjsonlog.console

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.intellij.execution.filters.ConsoleDependentInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.search.GlobalSearchScope
import io.github.orangain.prettyjsonlog.logentry.*
import io.github.orangain.prettyjsonlog.service.EphemeralStateService
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import com.google.gson.Gson
import io.github.orangain.prettyjsonlog.json.*

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
private val xmlPartPattern = Regex("<\\?xml.*?\\?>\\s*<([a-zA-Z_][\\w\\-.]*)(?:\\s[^>]*)?>.*?</\\1>", RegexOption.DOT_MATCHES_ALL)

class MyConsoleInputFilter(
    private val consoleView: ConsoleView,
    private val project: Project
) : InputFilter {
    override fun applyFilter(
        text: String,
        contentType: ConsoleViewContentType
    ): MutableList<Pair<String, ConsoleViewContentType>>? {
        thisLogger().debug("contentType: $contentType, applyFilter: $text")
        if (!isEnabled()) {
            return null
        }


        var logText = text
        var tooLarge: Boolean = false
        thisLogger().info("Log text length: ${text.length}, contentType: $contentType")
        if (text.length > (8000) && text.contains("@timestamp")) {
            thisLogger().warn("Log text is too large to parse: characters")
            tooLarge = true
            var textMessage: String? = null
            try {
                textMessage = extractFieldsFromText(text)
            } catch (e: Exception) {
                thisLogger().warn("Failed to extract fields from text: $text", e)
                textMessage = "Log too large to parse"
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
            val time = Regex("\"@timestamp\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(text)?.groups?.get(1)?.value ?: dateFormat.format(Date())
            val level = Regex("\"level\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(text)?.groups?.get(1)?.value ?: "UNKNOWN"
            val defaultText = """{"@timestamp": "${time}","level": "$level","message": "$textMessage"}"""
            logText = defaultText

        }

        if (parseJson(logText) == null) {
            thisLogger().debug("Log text is not a valid JSON: $logText")
            return null
        }

        val (node, suffixWhitespaces) = parseJson(logText) ?: return null
        thisLogger().debug("Parsed JSON node: $node")

        val timestamp = extractTimestamp(node)
        val level = extractLevel(node)
        val stackTrace = extractStackTrace(node)
        var message = extractMessage(node)?.replace("\n", " ") // Replace newlines in the message with spaces to avoid breaking the formatting.

        // .trimEnd('\n') is necessary because of the following reasons:
        // - When stackTrace is null or empty, we don't want to add an extra newline.
        // - When stackTrace ends with a newline, trimming the last newline makes a folding marker look better.
        val coloredMessage = "$level: $message\n${stackTrace ?: ""}".trimEnd('\n')

        var xmlPrettyPrintString = ""
        var jsonPartsPrettyString = ""
        try {
            if (message != null) {
                val xmlParts = xmlPartPattern.findAll(message)
                for (item in xmlParts.iterator()) {
                    val xmlString = item.groups[0]?.value.toString()
                    val onelineXML = xmlString.replace("\n", "")
                    if (message != null) {
                        message = message.replace(xmlString, onelineXML)
                    }
                    var xmlPString = prettifyXml(xmlString)
                    xmlPString = xmlPString.replace(Regex("\n[\\s]*\n"), "\n")
                    xmlPString = xmlPString.trimEnd('\n')
                    xmlPrettyPrintString += if (xmlPrettyPrintString.isEmpty()) xmlPString else "\n${xmlPString.trim()}"
                }
                if (xmlPrettyPrintString.isNotEmpty()) {
                    xmlPrettyPrintString = "\n${xmlPrettyPrintString.trim()}"
                }
            }

            if (!message.isNullOrEmpty()) {
                val jsonElements = extractJsonElements(message)
                jsonElements.forEach {
                    if (it.toString().contains(":")) {
                        thisLogger().debug("Found valid JSON element: $it")
                        val jsonNode: JsonNode? = getJsonNode(it.toString())
                        if (jsonNode != null) {
                            val jsonPString = prettyPrintJson(jsonNode)
                            jsonPartsPrettyString += if (jsonPartsPrettyString.isEmpty()) jsonPString else "\n$jsonPString"
                        }
                    }
                }
            }

            if (jsonPartsPrettyString.isNotEmpty()) {
                jsonPartsPrettyString = "\n${jsonPartsPrettyString.trim()}"
            }
        } catch (e: Exception) {
            thisLogger().warn("Failed to pretty print XML/JSON parts in the message: $message", e)
        }

        var prettyJsonString = prettyPrintJson(node)
        val jsonString = if (tooLarge) text else "$prettyJsonString$jsonPartsPrettyString$xmlPrettyPrintString"

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
