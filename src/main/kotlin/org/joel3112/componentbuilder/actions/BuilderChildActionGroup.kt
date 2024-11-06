package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import java.util.regex.Pattern

class BuilderChildActionGroup : DefaultActionGroup() {

    private fun getItems(e: AnActionEvent?): Pair<String, MutableList<Item>> {
        val settingsService = e?.project?.service<SettingsService>()
        val selectedLocation: VirtualFile? = e?.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        val selectedPath: String = selectedLocation?.path ?: ""

        val itemMatchRegex = settingsService?.items
            // Filter only parent items that are enabled
            ?.filter { it.isParent && it.enabled }
            // Find the first item that match the file path
            ?.firstOrNull {
                val matcher = Pattern.compile(
                    it.regexMatch
                ).matcher(selectedPath)

                matcher.matches()
            }

        if (itemMatchRegex != null) {
            val items = settingsService.getChildrenByItem(itemMatchRegex).filter { it.enabled }.toMutableList()
            return itemMatchRegex.name to items
        }
        return "" to mutableListOf()
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val actionGroupItems = mutableListOf<AnAction>()
        val (name, items) = getItems(e)
        actionGroupItems.add(Separator.create(name))
        items.forEach { item ->
            actionGroupItems.add(BuilderAction(item))
        }
        return actionGroupItems.toTypedArray()
    }


    override fun update(e: AnActionEvent) {
        val selectedLocation: VirtualFile? = e.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        if (selectedLocation?.isDirectory == true) {
            e.presentation.isVisible = false
        }

        val (name, items) = getItems(e)
        e.presentation.isEnabled = name.isNotEmpty() && items.isNotEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
