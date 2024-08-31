package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFile
import org.joel3112.componentbuilder.actions.components.CreateDialog
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.utils.BuilderCreator
import org.joel3112.componentbuilder.utils.IconUtils


class BuilderAction(val item: Item) : DumbAwareAction() {
    init {
        templatePresentation.text = item.name
        templatePresentation.icon = IconUtils.getIconByItem(item).second
    }

    private fun getLocation(file: VirtualFile): VirtualFile {
        return if (file.isDirectory) {
            file
        } else file.parent
    }

    private fun actionPerformedForChildFile(e: AnActionEvent) {
        val project = e.project!!

        val selectedLocation: VirtualFile? = e.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        val targetLocation = selectedLocation?.let { getLocation(it) }

        if (selectedLocation != null) {
            ApplicationManager.getApplication().runWriteAction(
                BuilderCreator(targetLocation!!, selectedLocation.nameWithoutExtension, item, project)
            )
        }
    }

    private fun actionPerformedForParentFile(e: AnActionEvent) {
        val project = e.project!!
        val dialog = CreateDialog(project, item)

        dialog.pack()
        dialog.show()
        if (dialog.isCanceled) {
            return
        }

        val selectedLocation: VirtualFile? = e.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        val targetLocation = selectedLocation?.let { getLocation(it) }

        ApplicationManager.getApplication().runWriteAction(
            BuilderCreator(targetLocation!!, dialog.getCName(), item, project)
        )
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (item.parent.isNotEmpty()) {
            actionPerformedForChildFile(e)
            return
        }
        actionPerformedForParentFile(e)
    }
}
