package org.joel3112.componentbuilder.settings.data

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "ComponentBuilderSettings",
    storages = [Storage("ComponentBuilderSettings.xml")]
)
class SettingsService : PersistentStateComponent<SettingsState> {

    private var settingsState: SettingsState = SettingsState()

    override fun getState(): SettingsState = settingsState

    override fun loadState(state: SettingsState) {
        settingsState = state
    }

    companion object {
        fun getInstance(project: Project): SettingsService {
            return project.getService(SettingsService::class.java)
        }
    }

    var items: MutableList<Item>
        get() = settingsState.items.toMutableList()
        set(value) {
            settingsState.items = value
        }
}
