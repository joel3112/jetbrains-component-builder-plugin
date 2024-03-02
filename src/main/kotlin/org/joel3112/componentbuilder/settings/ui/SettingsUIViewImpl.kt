package org.joel3112.componentbuilder.settings.ui

import com.intellij.util.ui.FormBuilder
import org.joel3112.componentbuilder.settings.data.SettingsState
import org.joel3112.componentbuilder.ui.settingsComponent.components.ItemsComponent
import org.joel3112.componentbuilder.ui.settingsComponent.components.NameComponent
import org.joel3112.componentbuilder.ui.settingsComponent.components.NameFormComponent
import javax.swing.JComponent

class SettingsUIViewImpl(initialState: SettingsState) : SettingsUIView {

    private var settingsState = initialState

    override var state: SettingsState
        set(value) {
            settingsState = value
            nameComponent.state = value.name
//            nameFormComponent.state = value.name
            itemsComponent.state = value.items
        }
        get() {
            val state = SettingsState()
            with(state) {
                name = nameComponent.state
//                name = nameFormComponent.state
                items = itemsComponent.state
            }
            return state
        }

    private lateinit var nameComponent: NameComponent
//    private lateinit var nameFormComponent: NameFormComponent
    private lateinit var itemsComponent: ItemsComponent

    override fun createComponent(): JComponent {
        nameComponent = NameComponent(settingsState.name)
//        nameFormComponent = NameFormComponent(settingsState.name)
        itemsComponent = ItemsComponent(settingsState.items)

        val formBuilder = FormBuilder.createFormBuilder()
        nameComponent.addToBuilder(formBuilder)
//        nameFormComponent.addToBuilder(formBuilder)
        itemsComponent.addToBuilder(formBuilder)

        return formBuilder.panel
    }
}
