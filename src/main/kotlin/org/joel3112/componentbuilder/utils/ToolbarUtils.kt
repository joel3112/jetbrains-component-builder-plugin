package org.joel3112.componentbuilder.utils

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.PopupUtil
import com.intellij.ui.AnActionButton
import com.intellij.ui.popup.PopupState
import javax.swing.Icon

class ToolbarUtils {
    companion object {

        fun createActionButton(
            text: String,
            icon: Icon,
            actionPerformed: (AnActionEvent) -> Unit,
            isEnabled: () -> Boolean,
        ): AnActionButton {
            return object : AnActionButton(text, icon) {
                override fun actionPerformed(e: AnActionEvent) = actionPerformed(e)
                override fun isEnabled(): Boolean = isEnabled()
                override fun getActionUpdateThread() = ActionUpdateThread.EDT
            }
        }

        @Suppress("UnstableApiUsage")
        fun actionsPopup(
            title: String,
            icon: Icon? = null,
            actions: List<AnAction>
        ): ActionGroup = object : ActionGroup(title, null, icon), DumbAware {

            private val popupState = PopupState.forPopup()

            init {
                isPopup = true
                templatePresentation.isPerformGroup = actions.isNotEmpty()
            }

            override fun getChildren(e: AnActionEvent?): Array<AnAction> = actions.toTypedArray()

            override fun actionPerformed(e: AnActionEvent) {
                if (popupState.isRecentlyHidden) {
                    return
                }

                val popup = JBPopupFactory
                    .getInstance()
                    .createActionGroupPopup(
                        null,
                        this,
                        e.dataContext,
                        JBPopupFactory.ActionSelectionAid.MNEMONICS,
                        true
                    )
                popupState.prepareToShow(popup)
                PopupUtil.showForActionButtonEvent(popup, e)
            }
        }
    }
}
