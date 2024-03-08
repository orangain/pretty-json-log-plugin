package io.github.orangain.prettyjsonlog.console

import com.intellij.execution.ConsoleFolding
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

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

private val prettyJsonPartRegex = Regex("""^([{}]| {2,}(".*": |}))""")
fun isPartOfPrettyJson(line: String): Boolean {
    return prettyJsonPartRegex.containsMatchIn(line)
}
