package org.joel3112.componentbuilder.settings.ui

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.joel3112.componentbuilder.settings.data.SettingsService
import javax.swing.JComponent

class BuilderSettingsConfigurable(private val project: Project) : SearchableConfigurable {

    private val settingsService: SettingsService = project.getService(SettingsService::class.java)
    private val settingsUIView: SettingsUIView = SettingsUIViewImpl(settingsService.state)


    override fun createComponent(): JComponent = settingsUIView.createComponent()

    override fun isModified(): Boolean = settingsService.state != settingsUIView.state

    override fun reset() {
        settingsUIView.state = settingsService.state
        settingsService.loadState(settingsService.state)
    }

    override fun apply() {
        val newState = settingsUIView.state
        settingsService.loadState(newState)
    }

    override fun getDisplayName(): String = "Component Builder"

    override fun getId(): String = "component-builder-settings"
}
