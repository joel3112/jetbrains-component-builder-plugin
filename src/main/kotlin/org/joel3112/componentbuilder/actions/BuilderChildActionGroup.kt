package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import java.util.regex.Pattern

class BuilderChildActionGroup : DefaultActionGroup() {

    private fun getItems(e: AnActionEvent?): MutableMap<String, MutableList<Item>> {
        val settingsService = e?.project?.service<SettingsService>()
        val variables = settingsService?.variables ?: mutableListOf()
        val selectedLocation: VirtualFile? = e?.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)

        val selectedName: String = selectedLocation?.nameWithoutExtension ?: ""
        val selectedPath: String = selectedLocation?.path ?: ""

        val itemsMatchFilePathFormatted = settingsService?.items
            // Filter only parent items that are enabled
            ?.filter {
                it.isParent && it.enabled
            }
            // Filter only items that match the file path
            ?.filter {
                val matcher = Pattern.compile(
                    it.regexPathFormatted(selectedName, variables)!!,
                    Pattern.CASE_INSENSITIVE
                ).matcher(selectedPath)

                matcher.matches()
            }

        if (itemsMatchFilePathFormatted == null) {
            return mutableMapOf()
        }

        val itemsMatchFilePathFormattedMap = mutableMapOf<String, MutableList<Item>>()
        itemsMatchFilePathFormatted.forEach { matchItem ->
            val items = settingsService.getChildrenByItem(matchItem).filter { it.enabled }.toMutableList()
            itemsMatchFilePathFormattedMap[matchItem.name] = items
        }

        return itemsMatchFilePathFormattedMap
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val actionGroupItems = mutableListOf<AnAction>()
        val itemsMap = getItems(e)
        itemsMap.forEach { (name, items) ->
            actionGroupItems.add(Separator.create(name))
            items.forEach { item ->
                actionGroupItems.add(BuilderAction(item))
            }
        }
        return actionGroupItems.toTypedArray()
    }


    override fun update(e: AnActionEvent) {
        val selectedLocation: VirtualFile? = e.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        if (selectedLocation?.isDirectory == true) {
            e.presentation.isVisible = false
        }

        e.presentation.isEnabled = getItems(e).isNotEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
