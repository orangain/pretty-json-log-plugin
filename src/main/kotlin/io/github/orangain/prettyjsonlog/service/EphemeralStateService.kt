package io.github.orangain.prettyjsonlog.service

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.components.Service
import java.util.*

@Service(Service.Level.PROJECT)
class EphemeralStateService {
    private val enabledMap = WeakHashMap<ConsoleView, Boolean>()

    /**
     * Returns true if the formatting is enabled for the given console view.
     */
    fun isEnabled(consoleView: ConsoleView): Boolean {
        return enabledMap[consoleView] ?: true // default is true
    }

    /**
     * Sets the enabled state of the formatting for the given console view.
     */
    fun setEnabled(consoleView: ConsoleView, value: Boolean) {
        enabledMap[consoleView] = value
    }
}
