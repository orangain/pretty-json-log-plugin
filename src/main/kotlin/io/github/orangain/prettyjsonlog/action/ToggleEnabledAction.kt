package io.github.orangain.prettyjsonlog.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAwareToggleAction
import io.github.orangain.prettyjsonlog.service.EphemeralStateService

class ToggleEnabledAction : DumbAwareToggleAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        val consoleView = e.getData(LangDataKeys.CONSOLE_VIEW) ?: return false
        val project = e.project ?: return false
        val service = project.service<EphemeralStateService>()
        return service.isEnabled(consoleView)
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        thisLogger().debug("setSelected: $state")
        val consoleView = e.getData(LangDataKeys.CONSOLE_VIEW) ?: return
        val project = e.project ?: return
        val service = project.service<EphemeralStateService>()
        service.setEnabled(consoleView, state)
    }
}
