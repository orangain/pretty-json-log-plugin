package io.github.orangain.prettyjsonlog.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame
import io.github.orangain.prettyjsonlog.MyBundle

internal class MyApplicationActivationListener : ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
        thisLogger().debug(MyBundle.message("applicationActivated"))
    }
}
