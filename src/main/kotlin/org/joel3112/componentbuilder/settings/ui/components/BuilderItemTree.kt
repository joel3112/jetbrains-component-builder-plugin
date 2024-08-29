package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.ui.AnActionButton
import com.intellij.ui.AnActionButtonRunnable
import com.intellij.ui.AnActionButtonUpdater
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.treeStructure.Tree
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import java.awt.Component
import java.util.stream.Stream
import javax.swing.JComponent
import javax.swing.JPanel
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
                icon = AllIcons.Nodes.Folder
                return this
            }

            font = font.deriveFont(java.awt.Font.PLAIN)
            if (item.icon.isNotEmpty()) {
                icon = item.icon.let {
                    AllIcons.FileTypes::class.java.getField(it).get(null)
                } as javax.swing.Icon
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
            myDecorator
                .setAddAction(createAddAction())
                .setRemoveAction(createRemoveAction())

            if (!isUpDownSupported()) {
                myDecorator.disableUpDownActions()
            }

            myDecorator.addExtraActions(*createExtraActions())
            val myPanel = myDecorator.createPanel()
            configureToolbarButtons(myPanel)
            return myPanel
        }

    init {
        myDecorator = createToolbarDecorator()

        val treeCellRenderer = ItemTreeCellRenderer()
        createNodes(itemsProperty.get())

        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        isEditable = false
        isRootVisible = false
        showsRootHandles = true
        model = DefaultTreeModel(root)
        setCellRenderer(treeCellRenderer)
    }

    private fun configureToolbarButtons(panel: JPanel) {
        val addButton = ToolbarDecorator.findAddButton(panel)
        val removeButton = ToolbarDecorator.findRemoveButton(panel)
        val editButton = ToolbarDecorator.findEditButton(panel)
        val upButton = ToolbarDecorator.findUpButton(panel)
        val downButton = ToolbarDecorator.findDownButton(panel)

        Stream.of<AnActionButton?>(addButton, removeButton, editButton, upButton, downButton)
            .filter { it: AnActionButton? -> it != null }
            .forEach { it: AnActionButton? ->
                it!!.addCustomUpdater(AnActionButtonUpdater { true })
            }
    }


    private fun createToolbarDecorator(): ToolbarDecorator {
        return ToolbarDecorator
            .createDecorator(this, null)
            .setToolbarPosition(ActionToolbarPosition.TOP)
    }


    private fun createRemoveAction(): AnActionButtonRunnable {
        return AnActionButtonRunnable { button -> removeSelectedNode() }
    }

    private fun createAddAction(): AnActionButtonRunnable {
        return AnActionButtonRunnable { button -> addNewNode() }
    }

    protected fun createExtraActions(): Array<AnActionButton> {
        return arrayOf()
    }

    private fun isUpDownSupported() = false

    private fun createNodes(items: MutableList<Item>) {
        items.forEach { item ->
            val node = DefaultMutableTreeNode(item)
            root.add(node)
        }
    }

    private fun addNewNode() {
        val root = model.root as DefaultMutableTreeNode
        val node = DefaultMutableTreeNode(Item())
        root.add(node)
        itemsProperty.set(treeItems)
        selectNodeOrLastNode(node)
    }

    private fun removeSelectedNode() {
        val selectedNode = lastSelectedPathComponent as DefaultMutableTreeNode
        val parent = selectedNode.parent as DefaultMutableTreeNode
        parent.remove(selectedNode)

        itemsProperty.set(treeItems)
        selectNodeOrLastNode(null)
    }

    private fun selectNodeOrLastNode(node: DefaultMutableTreeNode?) {
        if (node != null) {
            selectionPath = TreePath(node.path)
            return
        }
        val lastNode = root.children().toList().lastOrNull() as DefaultMutableTreeNode?
        if (lastNode != null) {
            selectionPath = TreePath(lastNode.path)
        }
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

    private fun refreshNode(item: Item) {
        val node = findNode(item)
        if (node != null) {
            node.userObject = item
            (model as DefaultTreeModel).nodeChanged(node)
        }
    }

    fun syncNodes() {
        updateUI()
        println("refreshNodes = settingsProperty:${settingsProperty.get().items.size} - treeItems:${treeItems.size}")

        if (settingsProperty.get().items.size != treeItems.size) {
            val settingsItemIds = settingsProperty.get().items.map { it.id }.toSet()
            val nodesToRemove = treeItems
                .filter { it.id !in settingsItemIds }
                .map { findNode(it) }
            nodesToRemove.forEach { node ->
                val parent = node?.parent as DefaultMutableTreeNode
                parent.remove(node)
            }
            selectNodeOrLastNode(null)
            return
        }

        settingsProperty.get().items.forEach { item ->
            refreshNode(item)
        }
    }
}

