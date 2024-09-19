package org.joel3112.componentbuilder.utils

import com.intellij.ui.CheckedTreeNode
import org.joel3112.componentbuilder.settings.data.Item

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
