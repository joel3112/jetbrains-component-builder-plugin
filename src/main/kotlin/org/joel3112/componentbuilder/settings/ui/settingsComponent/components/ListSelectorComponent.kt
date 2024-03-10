package org.joel3112.componentbuilder.settings.ui.settingsComponent.components

import com.intellij.ui.components.JBList
import com.intellij.util.ui.FormBuilder
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.MutableState
import org.joel3112.componentbuilder.settings.ui.settingsComponent.Component
import javax.swing.*

class ListSelectorComponent(private var listItems: List<Item>) : Component, MutableState<List<Item>> {
    private val list: JList<String> = JBList()

    init {
        list.setListData(listItems.map { it.name }.toTypedArray())
        list.selectedIndex = 0
        list.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): java.awt.Component {
                val itemName =
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                itemName.border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
                return itemName
            }
        }
    }

    val selectedItem: Item
        get() = listItems[list.selectedIndex]

    override fun addToBuilder(formBuilder: FormBuilder) {
        val panel = JPanel()
        panel.add(list)
        formBuilder.addLabeledComponent(panel, JLabel())
    }

    override var state: List<Item>
        get() = this.listItems
        set(items) {
            this.listItems = items
            list.setListData(items.map { it.name }.toTypedArray())
        }
}
