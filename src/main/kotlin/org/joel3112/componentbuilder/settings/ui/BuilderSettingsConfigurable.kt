package org.joel3112.componentbuilder.settings.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.util.preferredWidth
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.settings.data.SettingsState
import org.joel3112.componentbuilder.settings.ui.components.BuilderItemsEditor
import org.joel3112.componentbuilder.settings.ui.components.BuilderItemsTable
import javax.swing.JComponent

class BuilderSettingsConfigurable(project: Project) : SearchableConfigurable {

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
                    preferredWidth = JBUI.scale(200)
                }

            cell(itemsEditor.createPanel())
                .applyIfEnabled()
                .align(Align.FILL)

            with(itemsTable.tableView) {
                selectionModel.addListSelectionListener {
                    itemProperty.set(selectedObject)
                }

                val items = settingsProperty.get().items
                val firstSelected = if (items.size > 0) items.first() else itemProperty.get()
                itemsTable.tableView.selection = listOf(firstSelected)

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
