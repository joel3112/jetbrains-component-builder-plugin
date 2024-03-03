package org.joel3112.componentbuilder.settings.ui

import com.intellij.util.ui.FormBuilder
import org.joel3112.componentbuilder.settings.data.SettingsState
import org.joel3112.componentbuilder.ui.settingsComponent.components.ItemsComponent
import org.joel3112.componentbuilder.ui.settingsComponent.components.NameComponent
import javax.swing.JComponent

class SettingsUIViewImpl(initialState: SettingsState) : SettingsUIView {

    private var settingsState = initialState

    override var state: SettingsState
        set(value) {
            println("Setting set state ${value.name}")
            settingsState = value
            nameComponent.state = value.name
            itemsComponent.state = value.items
        }
        get() {
            val state = SettingsState()
            with(state) {
                println("Setting get state ${nameComponent.state}-${itemsComponent.state}")
                name = nameComponent.state
                items = itemsComponent.state
            }
            return state
        }

    private lateinit var nameComponent: NameComponent
    private lateinit var itemsComponent: ItemsComponent

    override fun createComponent(): JComponent {
        nameComponent = NameComponent(settingsState.name)
        itemsComponent = ItemsComponent(settingsState.items)

        val formBuilder = FormBuilder.createFormBuilder()
        nameComponent.addToBuilder(formBuilder)
        itemsComponent.addToBuilder(formBuilder)

        return formBuilder.panel
    }
}
