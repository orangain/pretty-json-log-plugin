package io.github.orangain.prettyjsonlog.console

import com.intellij.execution.ConsoleFolding
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import io.github.orangain.prettyjsonlog.json.isPartOfPrettyJson

class MyConsoleFolding : ConsoleFolding() {
    override fun getPlaceholderText(project: Project, lines: List<String>): String {
        return "{...}"
    }

    override fun shouldFoldLine(project: Project, line: String): Boolean {
        thisLogger().debug("shouldFoldLine: $line")
        return isPartOfPrettyJson(line)
    }

//    override fun shouldBeAttachedToThePreviousLine(): Boolean {
//        return false
//    }
}
