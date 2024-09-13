package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import java.util.regex.Pattern

class BuilderChildActionGroup : DefaultActionGroup() {

    private fun getItems(e: AnActionEvent?): MutableList<Item> {
        val settingsService = e?.project?.service<SettingsService>()
        val selectedLocation: VirtualFile? = e?.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)

        val selectedName: String = selectedLocation?.nameWithoutExtension ?: ""
        val selectedPath: String = selectedLocation?.path ?: ""

        val itemMatchFilePathFormatted = settingsService?.items
            ?.filter {
                it.isParent && it.enabled
            }
            ?.find {
                val matcher = Pattern.compile(
                    it.regexPathFormatted(selectedName)!!,
                    Pattern.CASE_INSENSITIVE
                ).matcher(selectedPath)

                matcher.matches()
            }

        if (itemMatchFilePathFormatted != null) {
            val enabledItems = settingsService.getChildrenByItem(itemMatchFilePathFormatted).filter {
                it.enabled
            }.toMutableList()
            return enabledItems
        }
        return mutableListOf()
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
        if (selectedLocation?.isDirectory == true) {
            e.presentation.isVisible = false
        }

        e.presentation.isEnabled = getItems(e).isNotEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
