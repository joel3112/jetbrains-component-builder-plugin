package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.ui.*
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.utils.IconUtils
import org.joel3112.componentbuilder.utils.TreeUtils
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
        val item = node.userObject as Item

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
    CheckPolicy(false, false, false, false)
) {
    private var myDecorator: ToolbarDecorator

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
        syncNodes()

        selectionModel.selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
        isRootVisible = false
        showsRootHandles = true
        model = DefaultTreeModel(root)
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
        val selectedNode = lastSelectedPathComponent as CheckedTreeNode
        val parentNode = selectedNode.parent as CheckedTreeNode
        val newItem = if (selectedNode.isParent) {
            Item(parent = (selectedNode.userObject as Item).id, enabled = selectedNode.isChecked)
        } else {
            Item(parent = (parentNode.userObject as Item).id, enabled = parentNode.isChecked)
        }
        itemsProperty.get().add(newItem)
        syncNodes()
        selectNode(findNode(newItem))
    }

    private fun addNewNode() {
        val newItem = Item()
        itemsProperty.get().add(newItem)
        syncNodes()
        selectNode(findNode(newItem))
    }

    private fun removeSelectedNode() {
        val selectedNode = lastSelectedPathComponent as CheckedTreeNode
        val item = selectedNode.userObject
        if (item is Item) {
            itemsProperty.get().remove(item)

            if (item.isParent) {
                val children = itemsProperty.get().filter { it.parent == item.id }
                children.forEach { childItem ->
                    itemsProperty.get().remove(childItem)
                }
            }
        }
        syncNodes()
        selectNode(null)
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

    fun selectNode(node: CheckedTreeNode?) {
        if (node != null) {
            selectionPath = TreePath(node.path)
            return
        }
        clearSelection()
    }

    fun findNode(item: Item): CheckedTreeNode? {
        val rootNode = model.root as CheckedTreeNode
        return rootNode.breadthFirstEnumeration().asSequence().map { it as CheckedTreeNode }
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


private val CheckedTreeNode.isParent: Boolean
    get() {
        return parent === root
    }
private val CheckedTreeNode.hasParentChecked: Boolean
    get() {
        if (isParent) {
            return isChecked
        }

        if (parent == null) {
            return false
        }

        val parentNode = parent as CheckedTreeNode
        return parentNode.isChecked
    }
