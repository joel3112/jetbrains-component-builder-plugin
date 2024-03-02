package org.joel3112.componentbuilder.settings.data

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "ComponentBuilderSettings",
    storages = [Storage("ComponentBuilderSettings.xml")]
)
class SettingsService : PersistentStateComponent<SettingsState> {

    private var settingsState: SettingsState = SettingsState()

    override fun getState(): SettingsState = settingsState

    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, settingsState)
    }
}
