package org.joel3112.componentbuilder.settings.ui

import com.intellij.util.ui.FormBuilder
import org.joel3112.componentbuilder.settings.data.SettingsState
import org.joel3112.componentbuilder.settings.ui.settingsComponent.components.ListSelectorComponent
import org.joel3112.componentbuilder.settings.ui.settingsComponent.components.NameComponent
import javax.swing.JComponent

class SettingsUIViewImpl(initialState: SettingsState) : SettingsUIView {

    private var settingsState = initialState

    override var state: SettingsState
        set(value) {
            settingsState = value
            nameComponent.state = value.name
//            itemsComponent.state = value.items
        }
        get() {
            val state = SettingsState()
            with(state) {
                name = nameComponent.state
//                items = itemsComponent.state
                println(listSelectorComponent.state)
                println(listSelectorComponent.selectedItem)
            }
            return state
        }

    private lateinit var nameComponent: NameComponent
    private lateinit var listSelectorComponent: ListSelectorComponent
//    private lateinit var itemsComponent: ItemsComponent

    override fun createComponent(): JComponent {
        nameComponent = NameComponent(settingsState.name)
        listSelectorComponent = ListSelectorComponent(settingsState.items)
//        itemsComponent = ItemsComponent(settingsState.items)

        val formBuilder = FormBuilder.createFormBuilder()
        nameComponent.addToBuilder(formBuilder)
        listSelectorComponent.addToBuilder(formBuilder)
//        itemsComponent.addToBuilder(formBuilder)

        return formBuilder.panel
    }
}
