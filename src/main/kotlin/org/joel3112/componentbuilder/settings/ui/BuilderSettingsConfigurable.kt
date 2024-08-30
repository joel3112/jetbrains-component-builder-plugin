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
import org.joel3112.componentbuilder.settings.ui.components.BuilderItemTree
import org.joel3112.componentbuilder.settings.ui.components.BuilderItemsEditor
import javax.swing.JComponent
import javax.swing.tree.DefaultMutableTreeNode

class BuilderSettingsConfigurable(project: Project) : SearchableConfigurable {

    private val settingsService = project.service<SettingsService>()
    private val propertyGraph = PropertyGraph()

    private val settingsProperty = propertyGraph.lazyProperty {
        SettingsService().apply {
            copyFrom(settingsService)
        }
    }
    private val itemProperty = propertyGraph.lazyProperty<Item?> { null }
        .apply {
            afterChange { item ->
                ApplicationManager.getApplication().invokeLater {
                }
                if (item != null) {
                    settingsProperty.set(
                        settingsProperty.get().apply {
                            items = items.map { if (it.id == item.id) item else it }.toMutableList()
                        }
                    )
                    itemsTree.refreshNode(item)
                }
            }
        }

    private val itemsTree = BuilderItemTree(settingsProperty)
    private val itemsEditor = BuilderItemsEditor(itemProperty, project)


    private val settingsPanel = panel {
        row {
            text(message("builder.settings.description"))
        }.bottomGap(BottomGap.SMALL)

        row {
            cell(itemsTree.component)
                .align(Align.FILL)
                .applyToComponent {
                    preferredWidth = JBUI.scale(200)
                }

            cell(itemsEditor.createPanel())
                .applyIfEnabled()
                .align(Align.FILL)

            with(itemsTree) {
                addTreeSelectionListener {
                    if (lastSelectedPathComponent != null) {
                        val node = lastSelectedPathComponent as DefaultMutableTreeNode
                        val selectedObject = node.userObject
                        if (selectedObject is Item) {
                            itemProperty.set(selectedObject)
                        }
                    } else {
                        itemProperty.set(null)
                    }
                }
                val items = settingsProperty.get().items
                val firstSelected = if (items.size > 0) items.first() else itemProperty.get()
                if (firstSelected != null) {
                    itemProperty.set(firstSelected)
                    setSelectionRow(0)
                }
            }
        }
    }

    override fun createComponent(): JComponent = settingsPanel

    override fun isModified(): Boolean {
//        println("isModified")
//        println("--> itemProperty: ${itemProperty.get()?.name} - ${itemProperty.get()?.template}")
//        println("--> settingsProperty: ${settingsProperty.get().items.map { it.name to it.template }}")
//        println("--> settingsService: ${settingsService.items.map { it.name to it.template }}")
//        println("====================================================================")

        return settingsProperty.get() != settingsService
    }

    override fun reset() {
        if (!isModified) return

        settingsProperty.set(SettingsService().apply {
            copyFrom(settingsService)
        })

        val items = settingsProperty.get().items
        itemProperty.set(if (items.isNotEmpty()) items.find { it.id == itemProperty.get()?.id } else null)

        ApplicationManager.getApplication().invokeLater {
            itemsTree.syncNodes()
            itemsTree.selectNodeOrLastNode(null)
        }
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
