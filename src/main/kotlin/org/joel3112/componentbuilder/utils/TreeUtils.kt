package org.joel3112.componentbuilder.utils

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.PopupUtil
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.popup.PopupState
import org.joel3112.componentbuilder.settings.data.Item
import javax.swing.Icon


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

val CheckedTreeNode.item: Item
    get() = userObject as Item

val CheckedTreeNode.isParent: Boolean
    get() = parent === root || this === root

val CheckedTreeNode.children: MutableList<CheckedTreeNode>?
    get() = if (isParent) children()?.toList()?.map { it as CheckedTreeNode }?.toMutableList() else null

val CheckedTreeNode.childrenCount: Int
    get() = if (isParent) children?.size ?: 0 else 0

val CheckedTreeNode.indexInParent: Int?
    get() = (parent as CheckedTreeNode).children?.indexOf(this)
