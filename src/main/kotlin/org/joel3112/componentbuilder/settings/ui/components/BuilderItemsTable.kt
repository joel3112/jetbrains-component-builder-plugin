package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.execution.util.ListTableWithButtons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

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
        if (itemsProperty.get().isNotEmpty()) {
            tableView.selection = listOf(itemsProperty.get().first())
        }
    }

    override fun createToolbarDecorator() = ToolbarDecorator
        .createDecorator(tableView, null)
        .setToolbarPosition(ActionToolbarPosition.TOP)

    override fun isUpDownSupported() = true
    override fun shouldEditRowOnCreation() = false
    override fun createListModel(): ListTableModel<*> = ListTableModel<Item>(NameColumn())
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

    private class NameColumn : ColumnInfo<Item, String>("") {
        override fun valueOf(item: Item?) = item?.name
    }

}
