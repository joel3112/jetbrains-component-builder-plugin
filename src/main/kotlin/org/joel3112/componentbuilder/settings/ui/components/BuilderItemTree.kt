package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.icons.AllIcons
import com.intellij.ide.dnd.*
import com.intellij.ide.dnd.aware.DnDAwareTree
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.ui.CheckboxTreeBase
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.ToolbarDecorator
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.DEFAULT_NAME
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.settings.data.createDefaultId
import org.joel3112.componentbuilder.utils.*
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

private class CheckBoxTreeCellRenderer : CheckboxTreeBase.CheckboxTreeCellRendererBase(true, false) {
    override fun customizeRenderer(
        tree: JTree?,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        val node = value as CheckedTreeNode
        val item = node.item

        textRenderer.isEnabled = node.isChecked
        textRenderer.icon = IconUtils.getIconByItem(item).second
        textRenderer.append(
            item.name,
            if (node.isParent) SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES else SimpleTextAttributes.REGULAR_ATTRIBUTES
        )
    }
}

private const val ROOT_NAME = "ROOT"
private var root: CheckedTreeNode = CheckedTreeNode(Item(ROOT_NAME))

class BuilderItemTree(
    private val settingsProperty: GraphProperty<SettingsService>,
) : CheckboxTreeBase(
    CheckBoxTreeCellRenderer(),
    root,
    CheckPolicy(true, true, true, false)
) {
    private var myDecorator: ToolbarDecorator
    private var onDroppedListener: ((draggedNode: CheckedTreeNode, newParentNode: CheckedTreeNode) -> Unit)? = null
    private var onStructureListener: ((node: CheckedTreeNode?) -> Unit)? = null

    private val itemsProperty = settingsProperty.transform(
        { it.items },
        {
            settingsProperty.get().apply {
                with(items) {
                    clear()
                    addAll(it)
                }
            }
        }
    )

    val lastSelectedPathComponent: CheckedTreeNode?
        get() = super.getLastSelectedPathComponent() as? CheckedTreeNode
    private val currentNodeSelected: CheckedTreeNode
        get() = lastSelectedPathComponent as CheckedTreeNode
    private val parentNodeSelected: CheckedTreeNode
        get() = currentNodeSelected.parent as CheckedTreeNode

    val component: JComponent
        get() {
            myDecorator.addExtraActions(*createExtraActions())
            val myPanel = myDecorator.createPanel()
            return myPanel
        }

    init {
        myDecorator = createToolbarDecorator()
        syncNodes()
        setupDragAndDrop()

        selectionModel.selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
        isRootVisible = false
        showsRootHandles = true
        model = DefaultTreeModel(root)
    }

    private fun setupDragAndDrop() {
        DnDSupport.createBuilder(this)
            .setBeanProvider {
                val nodeToDrag = currentNodeSelected
                if (!nodeToDrag.isParent || (nodeToDrag.isParent && nodeToDrag.childrenCount == 0)) {
                    DnDDragStartBean(nodeToDrag)
                } else null
            }
            .setImageProvider { info: DnDActionInfo ->
                val point = info.point
                val path = getPathForLocation(point.x, point.y)
                if (path != null) {
                    val image = DnDAwareTree.getDragImage(this, path, point).first
                    DnDImage(image)
                } else null
            }
            .setTargetChecker(object : DnDTargetChecker {
                override fun update(event: DnDEvent): Boolean {
                    val targetPath = getPathForLocation(event.point.x, event.point.y)
                    val isDropPossible = targetPath != null
                    event.isDropPossible = isDropPossible
                    return isDropPossible
                }
            })
            .setDropHandler { event ->
                val targetPath = getPathForLocation(event.point.x, event.point.y)
                val targetNode = targetPath?.lastPathComponent as? CheckedTreeNode
                val draggedNode = event.attachedObject as? CheckedTreeNode

                if (targetNode != null && draggedNode != null && targetNode.isParent) {
                    val currentParentNode = draggedNode.parent
                    if (currentParentNode != targetNode) {
                        val newParentItem = targetNode.item
                        val updatedItem = draggedNode.item.copy(parent = newParentItem.id)
                        val updatedNode = CheckedTreeNode(updatedItem)

                        onDroppedListener?.invoke(updatedNode, targetNode)
                        refreshAfterMutation(updatedNode)
                    }
                }
            }
            .install()
    }

    fun addTreeNodeDropListener(listener: (draggedNode: CheckedTreeNode, newParentNode: CheckedTreeNode) -> Unit) {
        onDroppedListener = listener
    }

    fun addTreeStructureChangeListener(listener: (nodes: CheckedTreeNode?) -> Unit) {
        onStructureListener = listener
    }

    private fun createToolbarDecorator(): ToolbarDecorator {
        return ToolbarDecorator
            .createDecorator(this, null)
            .setToolbarPosition(ActionToolbarPosition.TOP)
    }

    private fun createExtraActions(): Array<ActionGroup> {
        val buttons: ActionGroup = DefaultActionGroup().apply {
            val addButton = ToolbarUtils.createActionButton(
                message("builder.settings.tree.action.add.parent"),
                AllIcons.General.Add,
                { addNewNode() },
                { true }
            )

            val addChildButton = ToolbarUtils.createActionButton(
                message("builder.settings.tree.action.add.child"),
                AllIcons.Actions.AddFile,
                { addNewChildNode() },
                { lastSelectedPathComponent != null }
            )

            val removeButton = ToolbarUtils.createActionButton(
                message("builder.settings.tree.action.remove"),
                AllIcons.General.Remove,
                { if (selectionPaths.size == 1) removeSelectedNode() else removeSelectedNodes() },
                { lastSelectedPathComponent != null }
            )

            val duplicateButton = ToolbarUtils.createActionButton(
                message("builder.settings.tree.action.duplicate"),
                AllIcons.Actions.Copy,
                { duplicateSelectedNode() },
                { selectionCount == 1 && lastSelectedPathComponent?.isParent == false }
            )

            val upButton = ToolbarUtils.createActionButton(
                message("builder.settings.tree.action.up"),
                AllIcons.Actions.MoveUp,
                { moveUpSelectedNode() },
                {
                    if (selectionCount > 1 || lastSelectedPathComponent == null || lastSelectedPathComponent?.isParent == true) false
                    else lastSelectedPathComponent?.indexInParent != 0
                }
            )

            val downButton = ToolbarUtils.createActionButton(
                message("builder.settings.tree.action.down"),
                AllIcons.Actions.MoveDown,
                { moveDownSelectedNode() },
                {
                    if (selectionCount > 1 || lastSelectedPathComponent == null || (lastSelectedPathComponent?.isParent == true)) false
                    else lastSelectedPathComponent?.indexInParent != (lastSelectedPathComponent?.parent?.childCount!! - 1)
                }
            )

            val addGroupButton = ToolbarUtils.actionsPopup(
                title = message("builder.settings.tree.action.add"),
                icon = AllIcons.General.Add,
                actions = mutableListOf<AnAction>().apply {
                    add(addButton)
                    add(addChildButton)
                }
            )
            add(addGroupButton)
            add(removeButton)
            add(duplicateButton)
            add(upButton)
            add(downButton)
        }
        return arrayOf(buttons)
    }

    private fun addNewChildNode() {
        val newItem = if (currentNodeSelected.isParent) {
            Item(parent = currentNodeSelected.item.id, enabled = currentNodeSelected.isChecked)
        } else {
            Item(parent = parentNodeSelected.item.id, enabled = parentNodeSelected.isChecked)
        }
        itemsProperty.get().add(newItem)
        refreshAfterMutation(CheckedTreeNode(newItem))
    }

    private fun addNewNode() {
        val newItem = Item()
        itemsProperty.get().add(newItem)
        refreshAfterMutation(CheckedTreeNode(newItem))
    }

    private fun removeSelectedNodes() {
        val selectedNodes = selectionPaths.map { it.lastPathComponent as CheckedTreeNode }
        selectedNodes.forEach { removeSelectedNode(it) }
    }

    private fun removeSelectedNode(node: CheckedTreeNode = currentNodeSelected) {
        val removedItem = node.item
        itemsProperty.get().remove(removedItem)

        if (removedItem.isParent) {
            val children = itemsProperty.get().filter { it.parent == removedItem.id }
            children.forEach { childItem ->
                itemsProperty.get().remove(childItem)
            }
        }
        refreshAfterMutation(null)
    }

    private fun duplicateSelectedNode() {
        val newItem = currentNodeSelected.item.copy(
            id = createDefaultId(),
            name = DEFAULT_NAME
        )
        itemsProperty.get().add(newItem)
        refreshAfterMutation(CheckedTreeNode(newItem))
    }

    private fun moveUpSelectedNode() {
        val movedItem = currentNodeSelected.item
        val currentIndexInList = itemsProperty.get().indexOf(movedItem)

        itemsProperty.get()[currentIndexInList] = itemsProperty.get()[currentIndexInList - 1].also {
            itemsProperty.get()[currentIndexInList - 1] = itemsProperty.get()[currentIndexInList]
        }
        refreshAfterMutation(currentNodeSelected)
    }

    private fun moveDownSelectedNode() {
        val movedItem = currentNodeSelected.item
        val currentIndexInList = itemsProperty.get().indexOf(movedItem)

        itemsProperty.get()[currentIndexInList] = itemsProperty.get()[currentIndexInList + 1].also {
            itemsProperty.get()[currentIndexInList + 1] = itemsProperty.get()[currentIndexInList]
        }
        refreshAfterMutation(currentNodeSelected)
    }

    private fun expandAllNodes(node: TreeNode, path: TreePath) {
        for (i in 0 until node.childCount) {
            val childNode = node.getChildAt(i)
            val childPath = path.pathByAddingChild(childNode)
            expandPath(childPath)
            expandAllNodes(childNode, childPath)
        }
    }

    private fun expandAllNodes() {
        val root = model.root as CheckedTreeNode
        expandAllNodes(root, TreePath(root))
    }

    private fun findNodeByItem(item: Item): CheckedTreeNode? {
        val rootNode = model.root as CheckedTreeNode
        return rootNode.breadthFirstEnumeration().asSequence()
            .map { it as CheckedTreeNode }
            .find { node -> node.item.id == item.id }
    }

    private fun refreshAfterMutation(node: CheckedTreeNode?) {
        val item = node?.item
        syncNodes()
        selectNodeByItem(item)
        onStructureListener?.invoke(CheckedTreeNode(item))
    }

    fun selectNodeByItem(item: Item?) {
        if (item != null) {
            selectionPath = TreePath(findNodeByItem(item)!!.path)
            return
        }
        clearSelection()
    }

    fun refreshNodeByItem(item: Item) {
        val node = findNodeByItem(item)
        if (node != null && node.userObject != item) {
            node.userObject = item
            (model as DefaultTreeModel).nodeChanged(node)
        }
    }

    fun syncNodes() {
        root.removeAllChildren()

        val parentItems = settingsProperty.get().items.filter { it.isParent }
        parentItems.forEach { item ->
            val node = CheckedTreeNode(item)
            setNodeState(node, item.enabled)
            root.add(node)

            val childrenItems = settingsProperty.get().items.filter { it.parent == item.id }
            childrenItems.forEach { childItem ->
                val childNode = CheckedTreeNode(childItem)
                setNodeState(childNode, childItem.enabled)
                node.add(childNode)
            }
        }
        (model as DefaultTreeModel).reload()
        ApplicationManager.getApplication().invokeLater {
            expandAllNodes()
            updateUI()
        }
    }
}
