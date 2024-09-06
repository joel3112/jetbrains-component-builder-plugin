package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService

class BuilderParentActionGroup : DefaultActionGroup() {
    private fun getItems(e: AnActionEvent?): MutableList<Item> {
        val settingsService = e?.project?.service<SettingsService>()
        val enabledItems = settingsService?.getParentItems()?.filter {
            it.enabled
        }?.toMutableList()
        return enabledItems ?: mutableListOf()
    }


    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val actionGroupItems = mutableListOf<AnAction>()
        getItems(e).forEach { item ->
            actionGroupItems.add(BuilderAction(item))
        }

        return actionGroupItems.toTypedArray()
    }


    override fun update(e: AnActionEvent) {
        val selectedLocation: VirtualFile? = e.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        if (selectedLocation?.isDirectory == false) {
            e.presentation.isVisible = false
        }

        e.presentation.isEnabled = getItems(e).isNotEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
