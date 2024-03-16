package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.execution.util.ListTableWithButtons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import java.util.*
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
            tableHeader.isVisible = false
            model.addTableModelListener {
                itemsProperty.set(elements)
            }
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            setShowGrid(false)
            setValues(itemsProperty.get())
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
