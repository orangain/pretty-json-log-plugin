package io.github.orangain.prettyjsonlog.console

import com.intellij.execution.ConsoleFolding
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import io.github.orangain.prettyjsonlog.json.isPartOfPrettyJson
import io.github.orangain.prettyjsonlog.json.isPartOfPrettyXml
import io.github.orangain.prettyjsonlog.service.EphemeralStateService

class MyConsoleFolding : ConsoleFolding() {
    private var consoleView: ConsoleView? = null

    override fun getPlaceholderText(project: Project, lines: List<String>): String {
        return "{...}"
    }

    override fun shouldFoldLine(project: Project, line: String): Boolean {
        thisLogger().debug("shouldFoldLine: $line")
        if (!isEnabled(project)) {
            return false
        }
        return isPartOfPrettyJson(line) || isPartOfPrettyXml(line)
    }

    override fun isEnabledForConsole(consoleView: ConsoleView): Boolean {
        // This method "isEnabledForConsole" is not for storing consoleView, but we use it for that purpose because
        // there is no other way to get consoleView reference in "shouldFoldLine" method.
        this.consoleView = consoleView
        return true
    }

    private fun isEnabled(project: Project): Boolean {
        val service = project.service<EphemeralStateService>()
        val consoleView = this.consoleView ?: return false
        return service.isEnabled(consoleView)
    }

//    override fun shouldBeAttachedToThePreviousLine(): Boolean {
//        return false
//    }
}
