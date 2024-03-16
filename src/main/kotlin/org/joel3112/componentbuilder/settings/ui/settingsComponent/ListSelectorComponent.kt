package org.joel3112.componentbuilder.settings.ui.settingsComponent

import com.intellij.util.ui.FormBuilder
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.MutableState
import org.joel3112.componentbuilder.settings.ui.components.ListItemSelector
import javax.swing.*

class ListSelectorComponent(private var listItems: MutableList<Item>) : Component, MutableState<MutableList<Item>> {
     private val list: ListItemSelector = ListItemSelector()

    init {
        list.setValues(listItems)
        list.tableView.model.addTableModelListener {
            listItems = list.tableView.items
        }
    }

    val selectedItem: Item
        get() = list.tableView.selectedObject ?: Item("", "")

    fun addSelectionListener(listener: (Item) -> Unit) {
        list.tableView.selectionModel.addListSelectionListener {
            listener(selectedItem)
        }
    }

    fun selectItem(item: Item) {
        list.tableView.addSelection(item)
    }

    override fun addToBuilder(formBuilder: FormBuilder) {
        val panel = JPanel()
        panel.add(list.component)
        formBuilder.addComponent(panel)
    }

    override var state: MutableList<Item>
        get() = this.listItems
        set(items) {
            this.listItems = items
            list.setValues(items)
            this.selectItem(listItems.first())
        }
}
