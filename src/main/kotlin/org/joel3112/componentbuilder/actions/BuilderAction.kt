package org.joel3112.componentbuilder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import org.joel3112.componentbuilder.settings.data.Item

class BuilderAction (item: Item) : DumbAwareAction(
) {

    init {
        templatePresentation.text = item.name
    }

    override fun actionPerformed(e: AnActionEvent) {
        TODO("Not yet implemented")
    }
}
