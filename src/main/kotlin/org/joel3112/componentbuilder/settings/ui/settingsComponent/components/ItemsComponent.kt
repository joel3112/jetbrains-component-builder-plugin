package org.joel3112.componentbuilder.settings.ui.settingsComponent.components

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.MutableState
import org.joel3112.componentbuilder.settings.ui.settingsComponent.Component
import javax.swing.JLabel
import javax.swing.JPanel

class ItemsComponent(items: List<Item>) : Component, MutableState<List<Item>> {
    private val nameTextFields: MutableList<JBTextField> = ArrayList()

    init {
        for ((name) in items) {
            nameTextFields.add(JBTextField(name))
        }
    }

    override fun addToBuilder(formBuilder: FormBuilder) {
        val panel = JPanel()
        for (i in nameTextFields.indices) {
            val itemPanel = JPanel()
            itemPanel.add(JLabel("Item:" + (i + 1)))
            itemPanel.add(nameTextFields[i])
            panel.add(itemPanel)
        }
        formBuilder.addLabeledComponent(panel, JLabel())
    }

    override var state: List<Item>
        get() {
            val values: MutableList<Item> = ArrayList()
            for (i in nameTextFields.indices) {
                values.add(Item(nameTextFields[i].text))
            }
            return values
        }
        set(items) {
            for (i in items.indices) {
                nameTextFields[i].text = items[i].name
            }
        }
}
