package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService

class BuilderParentActionGroup : DefaultActionGroup() {
    private fun getItems(e: AnActionEvent?): MutableList<Item> {
        val settingsService = e?.project?.service<SettingsService>()
        return settingsService?.items?.filter { !it.isChildFile }?.toMutableList() ?: mutableListOf()
    }


    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val actionGroupItems = mutableListOf<AnAction>()
        getItems(e).forEach { item ->
            if (!item.isChildFile) {
                actionGroupItems.add(BuilderAction(item))
            }
        }

        return actionGroupItems.toTypedArray()
    }


    override fun update(e: AnActionEvent) {
        val selectedLocation: VirtualFile? = e.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        if (selectedLocation?.isDirectory == false) {
            e.presentation.isVisible = false
        }

        e.presentation.isEnabled = getItems(e).isNotEmpty() ?: false
    }
}
