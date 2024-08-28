package org.joel3112.componentbuilder.settings.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.GraphProperty
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
    private var itemsProperties: MutableList<GraphProperty<Item>> = settingsService.items.map { item ->
        propertyGraph.property(item)
    }.toMutableList()
    private val selectedItemProperty = propertyGraph
        .lazyProperty<Item?> { null }
        .apply {
            afterChange { item ->
//                ApplicationManager.getApplication().invokeLater {
//                    itemsTree.updateUI()
//                }
                if (item != null) {
                    val index = itemsProperties.indexOfFirst { it.get().id == item.id }
                    if (index != -1) {
                        itemsProperties[index].set(item!!)
                    }
                }
            }
        }

    private val itemsTree = BuilderItemTree(project)
    private val itemsEditor = BuilderItemsEditor(selectedItemProperty, project)


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
                            selectedItemProperty.set(selectedObject)
                        }
                    }
                }

                val items = settingsService.items
                val firstSelected = if (items.size > 0) items.first() else selectedItemProperty.get()
                if (firstSelected != null) {
                    selectedItemProperty.set(firstSelected)
                    setSelectionRow(0)
                }
            }
        }
    }

    override fun createComponent(): JComponent = settingsPanel

    override fun isModified(): Boolean {
        println("isModified")
        println("-----> itemsProperties -> ${itemsProperties.size} -> ${itemsProperties[0].get().name} - ${itemsProperties[0].get().template}")
        println()
        println("-----> selectedItemProperty -> ${selectedItemProperty.get()!!.name} - ${selectedItemProperty.get()!!.template}")
        println()
        println("-----> settingsService.items -> ${settingsService.items.size} -> ${settingsService.items[0].name} - ${settingsService.items[0].template}")
        println("****************************************")

        return settingsService.items.zip(itemsProperties).any { (originalItem, modifiedProperty) ->
            originalItem.name != modifiedProperty.get().name ||
                    originalItem.template != modifiedProperty.get().template ||
                    originalItem.isChildFile != modifiedProperty.get().isChildFile ||
                    originalItem.parentExtensions != modifiedProperty.get().parentExtensions ||
                    originalItem.icon != modifiedProperty.get().icon ||
                    originalItem.filePath != modifiedProperty.get().filePath
        }
    }

    override fun reset() {
        itemsProperties.forEachIndexed { index, itemProperty ->
            val serviceItem = settingsService.items.getOrNull(index)
            if (serviceItem != null) {
                if (selectedItemProperty.get()?.id == serviceItem.id) {
                    selectedItemProperty.set(serviceItem)
                }
                itemProperty.set(serviceItem.copy())
            } else {
                itemProperty.set(Item())
            }
        }

        println("reset")
        println("-----> itemsProperties -> ${itemsProperties.size} -> ${itemsProperties[0].get().name} - ${itemsProperties[0].get().template}")
        println()
        println("-----> selectedItemProperty -> ${selectedItemProperty.get()!!.name} - ${selectedItemProperty.get()!!.template}")
        println()
        println("-----> settingsService.items -> ${settingsService.items.size} -> ${settingsService.items[0].name} - ${settingsService.items[0].template}")
        println("****************************************")
    }

    override fun apply() {
        val updated = isModified
        settingsPanel.apply()
        if (updated) {
            settingsService.items = itemsProperties.map { it.get().copy() }.toMutableList()
        }
    }

    override fun getDisplayName(): String = message("builder.name")

    override fun getId(): String = "component-builder-settings"
}
