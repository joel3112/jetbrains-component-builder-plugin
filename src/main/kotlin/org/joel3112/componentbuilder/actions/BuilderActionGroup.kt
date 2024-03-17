package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import org.joel3112.componentbuilder.settings.data.SettingsService

class BuilderActionGroup : DefaultActionGroup() {
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val settingsService = e?.project?.service<SettingsService>()

        val actionGroupItems = mutableListOf<AnAction>()
        settingsService?.items?.forEach { item ->
            actionGroupItems.add(BuilderAction(item))
        }

        return actionGroupItems.toTypedArray()
    }


    override fun update(e: AnActionEvent) {
        val settingsService = e.project?.service<SettingsService>()

        e.presentation.isEnabled = settingsService?.items?.isNotEmpty() ?: false
    }
}
