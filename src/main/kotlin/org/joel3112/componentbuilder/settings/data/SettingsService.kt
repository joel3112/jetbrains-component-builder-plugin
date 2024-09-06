package org.joel3112.componentbuilder.settings.data

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.OptionTag

@Service(Service.Level.PROJECT)
@State(
    name = "ComponentBuilderSettings",
    storages = [Storage("ComponentBuilderSettings.xml")]
)
class SettingsService : SettingsState, BaseState(), PersistentStateComponent<SettingsService> {

    @get:OptionTag("ITEMS")
    override var items by list<Item>()

    override fun getState(): SettingsService = this

    override fun loadState(state: SettingsService) {
        copyFrom(state)
    }

    fun equalsState(state: SettingsService): Boolean {
        return this.items.hashCode() == state.items.hashCode()
    }

    fun getParentItems(): List<Item> {
        return items.filter { it.isParent }
    }

    fun getChildrenByItem(item: Item): List<Item> {
        return items.filter { it.parent == item.id }
    }
}
