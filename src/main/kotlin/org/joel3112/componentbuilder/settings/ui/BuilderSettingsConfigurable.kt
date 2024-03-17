package org.joel3112.componentbuilder.settings.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.settings.data.SettingsState
import org.joel3112.componentbuilder.settings.ui.components.BuilderItemsEditor
import org.joel3112.componentbuilder.settings.ui.components.BuilderItemsTable
import java.awt.Dimension
import javax.swing.JComponent

class BuilderSettingsConfigurable(private val project: Project) : SearchableConfigurable {

    private val settingsService = project.service<SettingsService>()
    private val propertyGraph = PropertyGraph()
    private val settingsProperty = propertyGraph.lazyProperty {
        SettingsService().apply {
            copyFrom(settingsService)
        }
    }
    private val itemProperty = propertyGraph
        .lazyProperty<Item?> { null }
        .apply {
            afterChange {
                ApplicationManager.getApplication().invokeLater {
                    itemsTable.tableView.updateUI()
                }
                settingsProperty.setValue(null, SettingsState::items, settingsProperty.get())
            }
        }

    private val itemsTable = BuilderItemsTable(settingsProperty)
    private val itemsEditor = BuilderItemsEditor(itemProperty, project)


    private val settingsPanel = panel {
        row {
            text(message("builder.settings.description"))
        }.bottomGap(BottomGap.SMALL)

        row {
            cell(itemsTable.component)
                .align(Align.FILL)
                .applyToComponent {
                    preferredSize = Dimension(200, 0)
                }

            cell(itemsEditor.createPanel())
                .applyIfEnabled()
                .align(Align.FILL)

            with(itemsTable.tableView) {
                itemProperty.set(itemProperty.get() ?: itemsTable.tableView.selectedObject)
                selectionModel.addListSelectionListener {
                    itemProperty.set(selectedObject)
                }
            }
        }
    }

    override fun createComponent(): JComponent = settingsPanel

    override fun isModified(): Boolean = settingsProperty.get() != settingsService

    override fun reset() {
        settingsProperty.set(settingsProperty.get().apply {
            copyFrom(settingsService)
        })
    }

    override fun apply() {
        val updated = isModified
        settingsPanel.apply()
        if (updated) {
            settingsService.copyFrom(settingsProperty.get())

        }
    }

    override fun getDisplayName(): String = message("builder.name")

    override fun getId(): String = "component-builder-settings"
}
