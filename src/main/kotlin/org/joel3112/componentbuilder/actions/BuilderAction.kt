package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFile
import org.joel3112.componentbuilder.actions.components.CreateDialog
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.utils.Creator

class BuilderAction(val item: Item) : DumbAwareAction(
    item.name,
    "Create a new ${item.name} ${if (item.isChildFile) "child" else ""} file",
    null
) {

    private fun getLocation(file: VirtualFile): VirtualFile {
        return if (file.isDirectory) {
            file
        } else file.parent
    }

    private fun actionPerformedForChildFile(e: AnActionEvent) {
        val selectedLocation: VirtualFile? = e.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        val targetLocation = selectedLocation?.let { getLocation(it) }

        if (selectedLocation != null) {
            ApplicationManager.getApplication().runWriteAction(
                Creator(targetLocation!!, selectedLocation.nameWithoutExtension, item)
            )
        }
    }

    private fun actionPerformedForParentFile(e: AnActionEvent) {
        val project = e.project!!
        val dialog = CreateDialog(project)

        dialog.pack()
        dialog.show()
        if (dialog.isCanceled) {
            return
        }

        val selectedLocation: VirtualFile? = e.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        val targetLocation = selectedLocation?.let { getLocation(it) }

        ApplicationManager.getApplication().runWriteAction(
            Creator(targetLocation!!, dialog.getCName(), item)
        )
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (item.isChildFile) {
            actionPerformedForChildFile(e)
            return
        }
        actionPerformedForParentFile(e)
    }
}
