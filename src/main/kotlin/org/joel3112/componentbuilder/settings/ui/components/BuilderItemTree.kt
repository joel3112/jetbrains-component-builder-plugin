package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.icons.AllIcons
import com.intellij.icons.ExpUiIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.treeStructure.Tree
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.utils.TreeUtils
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.tree.*

private const val ROOT_NAME = "ROOT"

private class ItemTreeCellRenderer : DefaultTreeCellRenderer() {
    override fun getTreeCellRendererComponent(
        tree: JTree?,
        value: Any,
        sel: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        super.getTreeCellRendererComponent(
            tree, value, sel, expanded, leaf, row, hasFocus
        )

        val node = value as DefaultMutableTreeNode
        if (node.userObject is Item) {
            val item = node.userObject as Item
            val isNodeParent = node.parent?.toString() == ROOT_NAME

            text = item.name
            if (isNodeParent) {
                font = font.deriveFont(java.awt.Font.BOLD)
                icon = ExpUiIcons.General.ListFiles
                return this
            }

            font = font.deriveFont(java.awt.Font.PLAIN)
            icon = if (item.icon.isNotEmpty()) {
                item.icon.let {
                    AllIcons.FileTypes::class.java.getField(it).get(null)
                } as javax.swing.Icon
            } else {
                ExpUiIcons.FileTypes.AnyType
            }
        }
        return this
    }
}


class BuilderItemTree(private val settingsProperty: GraphProperty<SettingsService>) : Tree() {
    private var myDecorator: ToolbarDecorator
    private var root: DefaultMutableTreeNode = DefaultMutableTreeNode(ROOT_NAME)

    private val treeItems
        get(): MutableList<Item> {
            val rootChildren = root.children().toList().map {
                it as DefaultMutableTreeNode
            }

            return rootChildren.map { it.userObject as Item }.toMutableList()
        }
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

    val component: JComponent
        get() {
            if (!isUpDownSupported()) {
                myDecorator.disableUpDownActions()
            }

            myDecorator.addExtraActions(*createExtraActions())
            val myPanel = myDecorator.createPanel()
            return myPanel
        }

    init {
        myDecorator = createToolbarDecorator()

        val treeCellRenderer = ItemTreeCellRenderer()
        syncNodes()

        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        isEditable = false
        isRootVisible = false
        showsRootHandles = true
        model = DefaultTreeModel(root)
        setCellRenderer(treeCellRenderer)
    }

    private fun createToolbarDecorator(): ToolbarDecorator {
        return ToolbarDecorator
            .createDecorator(this, null)
            .setToolbarPosition(ActionToolbarPosition.TOP)
    }

    protected fun createExtraActions(): Array<ActionGroup> {
        val buttons: ActionGroup = DefaultActionGroup().apply {
            val addButton: AnActionButton =
                object : AnActionButton("Add File Type", AllIcons.Actions.AddFile) {
                    override fun actionPerformed(e: AnActionEvent) {
                        addNewNode()
                    }

                    override fun isEnabled(): Boolean = true
                    override fun getActionUpdateThread() = ActionUpdateThread.EDT
                }

            val addChildButton: AnActionButton =
                object : AnActionButton("Add Child File Type", AllIcons.Actions.AddFile) {
                    override fun actionPerformed(e: AnActionEvent) {
                        addNewChildNode()
                    }

                    override fun isEnabled(): Boolean = lastSelectedPathComponent != null
                    override fun getActionUpdateThread() = ActionUpdateThread.EDT
                }

            val removeButton: AnActionButton =
                object : AnActionButton("Remove", AllIcons.General.Remove) {
                    override fun actionPerformed(e: AnActionEvent) {
                        removeSelectedNode()
                    }

                    override fun isEnabled(): Boolean = lastSelectedPathComponent != null
                    override fun getActionUpdateThread() = ActionUpdateThread.EDT
                }

            val addActions = mutableListOf<AnAction>().apply {
                add(addButton)
                add(addChildButton)
            }
            add(
                TreeUtils.actionsPopup(
                    title = "Add",
                    icon = AllIcons.General.Add,
                    actions = addActions
                )
            )
            add(removeButton)
        }
        return arrayOf(buttons)
    }

    private fun isUpDownSupported() = false


    private fun addNewChildNode() {
        val selectedNode = lastSelectedPathComponent as DefaultMutableTreeNode
        val parent = selectedNode.parent as DefaultMutableTreeNode
        val parentId = if ((parent.userObject is Item)) {
            (parent.userObject as Item).id
        } else {
            (selectedNode.userObject as Item).id
        }
        val newItem = Item(parent = parentId)
        itemsProperty.get().add(newItem)
        syncNodes()
        selectNodeOrLastNode(findNode(newItem))

    }

    private fun addNewNode() {
        val newItem = Item()
        itemsProperty.get().add(newItem)
        syncNodes()
        selectNodeOrLastNode(findNode(newItem))
    }

    private fun removeSelectedNode() {
        val selectedNode = lastSelectedPathComponent as DefaultMutableTreeNode
        val item = selectedNode.userObject
        if (item is Item) {
            itemsProperty.get().remove(item)

            val children = itemsProperty.get().filter { it.parent == item.id }
            children.forEach { childNode ->
                itemsProperty.get().remove(childNode)
            }
        }
        syncNodes()
        selectNodeOrLastNode(null)
    }

    fun selectNodeOrLastNode(node: DefaultMutableTreeNode?) {
        if (node != null) {
            selectionPath = TreePath(node.path)
            return
        }

        if (treeItems.isEmpty()) {
            clearSelection()
            return
        }

        val lastNode = findNode(treeItems.last())
        if (lastNode != null) {
            selectionPath = TreePath(lastNode.path)
        }
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
        val root = model.root as DefaultMutableTreeNode
        expandAllNodes(root, TreePath(root))
    }

    private fun findNode(item: Item): DefaultMutableTreeNode? {
        val rootNode = model.root as DefaultMutableTreeNode
        return rootNode.breadthFirstEnumeration().asSequence().map { it as DefaultMutableTreeNode }
            .find { node ->
                val selectedObject = node.userObject
                if (selectedObject is Item) {
                    (node.userObject as Item).id == item.id
                } else {
                    false
                }
            }
    }

    fun refreshNode(item: Item) {
        val node = findNode(item)
        if (node != null && node.userObject != item) {
            node.userObject = item
            (model as DefaultTreeModel).nodeChanged(node)
        }
    }

    fun syncNodes() {
        root.removeAllChildren()

        val parentItems = settingsProperty.get().items.filter { it.parent.isEmpty() }
        parentItems.forEach { item ->
            val node = DefaultMutableTreeNode(item)
            root.add(node)

            val childrenItems = settingsProperty.get().items.filter { it.parent == item.id }
            childrenItems.forEach { childItem ->
                val childNode = DefaultMutableTreeNode(childItem)
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

