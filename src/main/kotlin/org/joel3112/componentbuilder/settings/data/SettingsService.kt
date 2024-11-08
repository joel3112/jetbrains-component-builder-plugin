package org.joel3112.componentbuilder.settings.data

import VariablesResolver
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.OptionTag


@Service(Service.Level.PROJECT)
@State(
    name = "ComponentBuilderSettings",
    defaultStateAsResource = true,
    storages = [Storage("ComponentBuilderSettings.xml")]
)
class SettingsService : SettingsState, BaseState(), PersistentStateComponent<SettingsService> {

    @get:OptionTag("ITEMS")
    override var items by list<Item>()

    @get:OptionTag("VARIABLES")
    override var variables by list<Variable>()

    override fun getState(): SettingsService = this

    override fun loadState(state: SettingsService) {
        val defaultVariable = VariablesResolver.defaultVariable
        val variablesHasDefault = state.variables.find { it.name == defaultVariable.name } != null
        if (!variablesHasDefault) {
            state.variables.add(defaultVariable)
        }
        copyFrom(state)
    }

    fun equalsState(state: SettingsService): Boolean {
        return this.items.hashCode() == state.items.hashCode() && this.variables.hashCode() == state.variables.hashCode()
    }

    fun getParentItems(): List<Item> {
        return items.filter { it.isParent }
    }

    fun getChildrenByItem(item: Item): List<Item> {
        return items.filter { it.parent == item.id && it.filePath.isNotEmpty() }
    }
}
