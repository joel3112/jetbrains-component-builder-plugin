package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.execution.util.ListTableWithButtons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import org.joel3112.componentbuilder.settings.data.Item
import java.util.*
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

class ListItemSelector : ListTableWithButtons<Item>() {

    init {
        tableView.apply {
            border = JBUI.Borders.empty(10, 30, 100, 20)
            columnSelectionAllowed = false
            tableHeader.reorderingAllowed = false
            tableHeader.isVisible = false
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            setShowGrid(false)
        }
    }

    override fun createToolbarDecorator() = ToolbarDecorator
        .createDecorator(tableView, null)
        .setToolbarPosition(ActionToolbarPosition.TOP)
        .setPanelBorder(JBUI.Borders.empty())

    override fun isUpDownSupported() = true
    override fun shouldEditRowOnCreation() = false
    override fun createListModel(): ListTableModel<*> = ListTableModel<Item>(NameColumn())
    override fun createElement() = Item(UUID.randomUUID().toString(), "Unnamed")
    override fun addNewElement(newElement: Item) {
        super.addNewElement(newElement)
        SwingUtilities.invokeLater {
            tableView.selection = listOf(newElement)
        }
    }

    override fun canDeleteElement(selection: Item) = true
    override fun cloneElement(variable: Item) = variable.copy()
    override fun isEmpty(element: Item): Boolean = element.name.isEmpty()

    private class NameColumn : ColumnInfo<Item, String>("Name") {
        override fun valueOf(item: Item?) = item?.name
    }

}
