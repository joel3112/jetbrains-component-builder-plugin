package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.execution.util.ListTableWithButtons
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import java.awt.Component
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableCellRenderer

class BuilderItemsTable(private val settingsProperty: ObservableMutableProperty<SettingsService>) :
    ListTableWithButtons<Item>() {

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

    init {
        tableView.apply {
            columnSelectionAllowed = false
            tableHeader.reorderingAllowed = false
            rowHeight = JBUI.scale(24)
            columnModel.getColumn(0).apply {
                maxWidth = JBUI.scale(22)
                minWidth = JBUI.scale(22)
            }
            model.addTableModelListener {
                itemsProperty.set(elements)
            }
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            setShowGrid(false)
            setValues(itemsProperty.get())
        }
        itemsProperty.afterChange {
            refreshValues()
        }
    }

    override fun createToolbarDecorator() = ToolbarDecorator
        .createDecorator(tableView, null)
        .setToolbarPosition(ActionToolbarPosition.TOP)

    override fun isUpDownSupported() = true
    override fun shouldEditRowOnCreation() = false
    override fun createListModel(): ListTableModel<*> = ListTableModel<Item>(IconColumn(), NameColumn())
    override fun createElement() = Item()
    override fun addNewElement(newElement: Item) {
        super.addNewElement(newElement)
        SwingUtilities.invokeLater {
            tableView.selection = listOf(newElement)
        }
    }

    override fun canDeleteElement(selection: Item) = true
    override fun cloneElement(variable: Item) = variable.copy()
    override fun isEmpty(element: Item): Boolean = element.name.isEmpty()

    private class IconColumn : ColumnInfo<Item, String>("") {
        override fun getName() = ""
        override fun valueOf(item: Item?) = ""
        override fun getRenderer(item: Item?) = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: JTable?,
                value: Any?,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    .apply {
                        if (item != null && item.icon.isNotEmpty()) {
                            icon = item.icon.let {
                                AllIcons.FileTypes::class.java.getField(it).get(null)
                            } as javax.swing.Icon
                        }
                    }
            }
        }
    }

    private class NameColumn : ColumnInfo<Item, String>("") {
        override fun getName() = ""
        override fun valueOf(item: Item?) = item?.name
        override fun getRenderer(item: Item?) = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: JTable?,
                value: Any?,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    .apply {
                        if (item != null) {
                            font = font.deriveFont(if (!item.isChildFile) java.awt.Font.BOLD else java.awt.Font.PLAIN)
                        }
                    }
            }
        }
    }

}
