package org.joel3112.componentbuilder.settings.ui

import com.intellij.util.ui.FormBuilder
import org.joel3112.componentbuilder.settings.data.SettingsState
import org.joel3112.componentbuilder.settings.ui.settingsComponent.ConfigurationComponent
import org.joel3112.componentbuilder.settings.ui.settingsComponent.ListSelectorComponent
import javax.swing.JComponent

class SettingsUIViewImpl(initialState: SettingsState) : SettingsUIView {

    private var settingsState = initialState

    override var state: SettingsState
        set(value) {
            settingsState = value
            listSelectorComponent.state = value.items
            configurationComponent.state = listSelectorComponent.selectedItem
        }
        get() {
            val state = SettingsState()
            with(state) {
                items = listSelectorComponent.state
//                val itemChanged = items.find { it.id == configurationComponent.state.id }
//                if (itemChanged != null) {
//                    itemChanged.name = configurationComponent.state.name
//                }

            }
            return state
        }

    private lateinit var listSelectorComponent: ListSelectorComponent
    private lateinit var configurationComponent: ConfigurationComponent

    override fun createComponent(): JComponent {
        listSelectorComponent = ListSelectorComponent(settingsState.items)
        listSelectorComponent.selectItem(settingsState.items.first())
        listSelectorComponent.addSelectionListener {
            configurationComponent.state = listSelectorComponent.selectedItem
        }

        configurationComponent = ConfigurationComponent(settingsState.items.first())
        configurationComponent.addNameChangeListener { item, name ->
            if (listSelectorComponent.selectedItem.name != name) {
                listSelectorComponent.state = listSelectorComponent.state.map {
                    if (it.id == item.id) {
                        it.name = name
                    }
                    it
                }.toMutableList()
            }
            listSelectorComponent.selectItem(item)
        }

        val formBuilder = FormBuilder.createFormBuilder()
        listSelectorComponent.addToBuilder(formBuilder)
        configurationComponent.addToBuilder(formBuilder)

        return formBuilder.panel
    }
}
