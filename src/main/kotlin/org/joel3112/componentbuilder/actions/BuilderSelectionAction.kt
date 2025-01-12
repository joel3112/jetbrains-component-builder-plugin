package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.actions.components.SaveDialog
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.utils.IconUtils
import org.joel3112.componentbuilder.utils.NotificationUtils
import java.util.regex.Pattern

class BuilderSelectionAction : DumbAwareAction() {
    private fun getItems(e: AnActionEvent): List<Item> {
        val settingsService = e.project?.service<SettingsService>()
        val selectedLocation: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val selectedPath: String = selectedLocation?.path ?: ""

        val itemsSameExtension: List<Item> = sortByParent(settingsService!!.items)
            .filter { it.filePath.isNotEmpty() }
            .filter {
                val fileExtension = IconUtils.getIconByItem(Item(filePath = selectedPath)).first
                val itemExtension = IconUtils.getIconByItem(it).first
                fileExtension == itemExtension
            }
            .map {
                it.copy(name = generateLabel(it, settingsService))
            }
        return itemsSameExtension
    }

    private fun generateLabel(item: Item, settingsService: SettingsService): String {
        val parent = settingsService.items.find { it.id == item.parent }
        return if (item.isParent) {
            item.name
        } else if (parent != null) {
            "${parent.name} > ${item.name}"
        } else {
            item.name
        }
    }

    private fun sortByParent(items: List<Item>): List<Item> {
        // Group items by their parent ID
        val parentToChildrenMap = items.groupBy { it.parent }

        // Result list to maintain the sorted order
        val sortedList = mutableListOf<Item>()

        // Recursive function to add items and their children
        fun addItemAndChildren(item: Item) {
            sortedList.add(item) // Add the current item
            // Add all children of the current item
            parentToChildrenMap[item.id]?.forEach { child ->
                addItemAndChildren(child)
            }
        }

        // Add all top-level items (parent is empty) and their descendants
        parentToChildrenMap[""]?.forEach { parentItem ->
            addItemAndChildren(parentItem)
        }

        return sortedList
    }


    private fun getSelectedText(e: AnActionEvent, selectedLocation: VirtualFile?): String {
        val project = e.project!!
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val selectionModel: SelectionModel = editor!!.selectionModel

        val selectedText: String = if (selectionModel.hasSelection()) {
            selectionModel.selectedText ?: ""
        } else {
            if (selectedLocation != null) {
                val psiFile = PsiManager.getInstance(project).findFile(selectedLocation)
                psiFile?.text ?: ""
            } else {
                ""
            }
        }

        return selectedText
    }

    private fun updateSelection(e: AnActionEvent, selectedItem: Item?, updatedTemplate: String) {
        val settingsService = e.project?.service<SettingsService>()
        val currentItems = settingsService?.items ?: return

        val updatedItems = currentItems.map { item ->
            if (item.id == selectedItem!!.id) {
                item.copy(template = updatedTemplate)
            } else {
                item
            }
        }

        settingsService.items = updatedItems.toMutableList()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val selectedLocation: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val selectedPath: String = selectedLocation?.path ?: ""
        val selectedText: String = getSelectedText(e, selectedLocation)

        val itemsSameExtension: List<Item> = getItems(e)
        val itemMatchRegex: Item? = getItems(e)
            .filter { it.isParent }
            .firstOrNull {
                val matcher = Pattern.compile(
                    it.regexMatch
                ).matcher(selectedPath)

                matcher.matches()
            }

        val dialog = SaveDialog(e.project!!, selectedText, itemMatchRegex, itemsSameExtension)

        dialog.pack()
        dialog.show()
        if (dialog.isCanceled) {
            return
        }

        val updatedTemplate = dialog.ctemplate
        val selectedItem = dialog.cselectedItem
        updateSelection(e, selectedItem, updatedTemplate)
        NotificationUtils.notifyInfo(message("builder.notification.save.success", dialog.cselectedItem!!.name), e.project!!)
    }


    override fun update(e: AnActionEvent) {
        val items = getItems(e)
        e.presentation.isEnabled = items.isNotEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}