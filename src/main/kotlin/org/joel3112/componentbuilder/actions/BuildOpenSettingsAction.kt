package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import org.joel3112.componentbuilder.settings.ui.BuilderSettingsConfigurable


class BuildOpenSettingsAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        ShowSettingsUtil.getInstance().showSettingsDialog(project, BuilderSettingsConfigurable::class.java)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}