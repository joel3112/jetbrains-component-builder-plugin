package org.joel3112.componentbuilder.settings.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.CheckboxTreeListener
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.settings.data.sortByParent
import org.joel3112.componentbuilder.settings.ui.components.BuilderItemTree
import org.joel3112.componentbuilder.settings.ui.components.BuilderItemsEditor
import org.joel3112.componentbuilder.utils.item
import org.joel3112.componentbuilder.utils.preferredWidth
import javax.swing.JComponent

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
                    val itemInSettings = settingsProperty.get().items.find { it.id == item.id }
                    if (!item.equals(itemInSettings)) {
                        settingsProperty.set(
                            settingsProperty.get().apply {
                                items = items.map { if (it.id == item.id) item else it }.toMutableList()
                            }
                        )
                        itemsTree.refreshNodeByItem(item)
                    }
                }
            }
        }

    private val itemsTree = BuilderItemTree(settingsProperty)
    private val itemsEditor = BuilderItemsEditor(settingsProperty, itemProperty, project)


    private val settingsPanel = panel {
        row {
            text(message("builder.settings.description"))
        }.bottomGap(BottomGap.SMALL)

        row {
            cell(itemsTree.component)
                .align(Align.FILL)
                .applyToComponent {
                    preferredWidth(JBUI.scale(200))
                }

            cell(itemsEditor.createPanel())
                .applyIfEnabled()
                .align(Align.FILL)

            with(itemsTree) {
                addTreeSelectionListener {
                    if (lastSelectedPathComponent != null) {
                        val node = lastSelectedPathComponent
                        itemProperty.set(node?.item)
                    } else {
                        itemProperty.set(null)
                    }
                }

                addTreeStructureChangeListener {
                    sortItems()
                }

                addCheckboxTreeListener(object : CheckboxTreeListener {
                    override fun nodeStateChanged(node: CheckedTreeNode) {
                        val itemChanged = node.item

                        ApplicationManager.getApplication().invokeLater {
                            settingsProperty.get().apply {
                                items = items.map {
                                    if (it.id == itemChanged.id) it.copy(
                                        enabled = node.isChecked,
                                        id = it.id,
                                        parent = it.parent,
                                        name = it.name,
                                        filePath = it.filePath,
                                        template = it.template,
                                    ) else it
                                }.toMutableList()
                            }
                        }
                    }
                })

                addTreeNodeDropListener { draggedNode, _ ->
                    itemProperty.set(draggedNode.item)
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

    private fun sortItems() {
        settingsProperty.set(
            settingsProperty.get().apply {
                items = items.sortByParent()
            }
        )
    }

    override fun createComponent(): JComponent = settingsPanel

    override fun isModified(): Boolean {
        return !settingsProperty.get().equalsState(settingsService)
    }

    override fun reset() {
        if (!isModified) return

        settingsProperty.set(SettingsService().apply {
            copyFrom(settingsService)
        })

        val items = settingsProperty.get().items
        itemProperty.set(if (items.isNotEmpty()) items.find { it.id == itemProperty.get()?.id } else null)

        itemsTree.syncNodes()
        itemsTree.selectNodeByItem(null)
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
