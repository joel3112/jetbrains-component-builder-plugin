package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.icons.AllIcons.FileTypes
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.util.preferredHeight
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item


class BuilderItemsEditor(
    val itemProperty: GraphProperty<Item?>,
    val project: Project
) :
    UiDslUnnamedConfigurable.Simple() {

    private lateinit var isChildFileCheckBox: Cell<JBCheckBox>
    private lateinit var parentExtensionsTextField: Cell<JBTextField>
    private lateinit var nameTextField: Cell<JBTextField>
    private lateinit var iconComboBox: Cell<ComboBox<String>>
    private lateinit var filePathTextField: Cell<JBTextField>
    private lateinit var templateBuilderEditor: Cell<BuilderEditor>

    private val allIconsList = FileTypes::class.java.fields.map { it.name }

    private val selectedRowPredicate = object : ComponentPredicate() {
        override fun invoke() = itemProperty.isNotNull().get()

        override fun addListener(listener: (Boolean) -> Unit) =
            itemProperty.afterChange {
                listener(it != null)
            }
    }

    private val isChildFilePredicate = object : ComponentPredicate() {
        override fun invoke() = itemProperty.get()?.isChildFile ?: false

        override fun addListener(listener: (Boolean) -> Unit) =
            itemProperty.afterChange {
                listener(it?.isChildFile ?: false)
            }
    }

    private val propertyGraph = PropertyGraph()
    private val isChildFileProperty: GraphProperty<Boolean> = propertyGraph.property(false)
    private val parentExtensionsProperty: GraphProperty<String> = propertyGraph.property("")
    private val nameProperty: GraphProperty<String> = propertyGraph.property("")
    private val iconProperty: GraphProperty<String> = propertyGraph.property("")
    private val filePathProperty: GraphProperty<String> = propertyGraph.property("")
    private val templateProperty: GraphProperty<String> = propertyGraph.property("")

    init {
        itemProperty.afterChange { item ->
            if (item != null) {
                isChildFileCheckBox.component.isSelected = item.isChildFile
                parentExtensionsTextField.component.text = item.parentExtensions
                nameTextField.component.text = item.name
                iconComboBox.component.selectedIndex = allIconsList.indexOf(item.icon)
                filePathTextField.component.text = item.filePath
                templateBuilderEditor.component.setText(item.template)
            }
        }

        isChildFileProperty.afterChange { isChildFile ->
            itemProperty.set(
                itemProperty.get()?.copy(isChildFile = isChildFile)
            )
        }
        parentExtensionsProperty.afterChange { newParentExtensions ->
            itemProperty.set(
                itemProperty.get()?.copy(parentExtensions = newParentExtensions)
            )
        }
        nameProperty.afterChange { newName ->
            itemProperty.set(
                itemProperty.get()?.copy(name = newName)
            )
        }
        iconProperty.afterChange { newIcon ->
            itemProperty.set(
                itemProperty.get()?.copy(icon = newIcon)
            )
        }
        filePathProperty.afterChange { newFilePath ->
            itemProperty.set(
                itemProperty.get()?.copy(filePath = newFilePath)
            )
        }
        templateProperty.afterChange { newTemplate ->
            itemProperty.set(
                itemProperty.get()?.copy(template = newTemplate)
            )
        }
    }


    override fun Panel.createContent() {
        panel {
            row {
                panel {
                    row {
                        isChildFileCheckBox = checkBox(message("builder.settings.isChildFile"))
                            .bindSelected(isChildFileProperty)
                            .applyToComponent {
                                addActionListener {
                                    if (!isSelected) {
                                        parentExtensionsTextField.component.text = ""
                                    }
                                }
                            }.gap(RightGap.COLUMNS)

                        parentExtensionsTextField = expandableTextField()
                            .label(message("builder.settings.parentExtensions"), LabelPosition.LEFT)
                            .columns(COLUMNS_SHORT)
                            .bindText(parentExtensionsProperty)
                            .enabledIf(isChildFilePredicate)
                    }.bottomGap(BottomGap.NONE)

                    row {
                        comment(message("builder.settings.isChildFile.legend"), 60)
                    }
                }
            }
                .bottomGap(BottomGap.SMALL)

            group(message("builder.settings.group.display")) {
                row {
                    comment(message("builder.settings.group.display.description"))
                }

                row {
                    nameTextField = textField()
                        .label(message("builder.settings.name"), LabelPosition.TOP)
                        .bindText(nameProperty)

                    iconComboBox = comboBox(allIconsList)
                        .label(message("builder.settings.icon"), LabelPosition.TOP)
                        .bindItem(iconProperty)
                        .applyToComponent {
                            selectedIndex = -1
                            renderer = listCellRenderer { label ->
                                text = label
                                icon = FileTypes::class.java.getField(label).get(null) as javax.swing.Icon
                            }
                        }
                }
            }

            group(message("builder.settings.group.file")) {
                row {
                    comment(message("builder.settings.group.file.description"))
                }

                row(message("builder.settings.filePath")) {
                    filePathTextField = expandableTextField()
                        .comment(message("builder.settings.filePath.legend"), 50)
                        .columns(COLUMNS_LARGE)
                        .bindText(filePathProperty)
                }

                row {
                    templateBuilderEditor = cell(BuilderEditor(project, templateProperty))
                        .label(message("builder.settings.template"), LabelPosition.TOP)
                        .align(AlignX.FILL)
                        .applyToComponent {
                            preferredHeight = JBUI.scale(200)
                        }
                }
            }
        }.enabledIf(selectedRowPredicate)
    }
}
