package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.observable.util.transform
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
import org.joel3112.componentbuilder.utils.FileUtils
import org.joel3112.componentbuilder.utils.IconUtils
import javax.swing.JTextArea
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty1


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
    private lateinit var templateTextArea: Cell<JTextArea>

    private val allIconsList = IconUtils.getIconList()

    private val selectedRowPredicate = object : ComponentPredicate() {
        override fun invoke() = itemProperty.isNotNull().get()

        override fun addListener(listener: (Boolean) -> Unit) =
            itemProperty.afterChange {
                listener(it != null)
            }
    }

    private val isChildFilePredicate = object : ComponentPredicate() {
        override fun invoke() = itemProperty.get()?.parent?.isNotEmpty() ?: false

        override fun addListener(listener: (Boolean) -> Unit) =
            itemProperty.afterChange {
                listener(it?.parent?.isNotEmpty() ?: false)
            }
    }

    override fun Panel.createContent() {
        panel {
            row {
                panel {
                    row {
                        comment(message("builder.settings.isChildFile.legend"), 60)
                    }
                }
            }
                .visibleIf(isChildFilePredicate)
                .bottomGap(BottomGap.SMALL)

            group(message("builder.settings.group.display")) {
                row {
                    comment(message("builder.settings.group.display.description"))
                }

                row {
                    nameTextField = textField()
                        .label(message("builder.settings.name"), LabelPosition.TOP)
                        .bindText(itemProperty, Item::name)

                    iconComboBox = comboBox(allIconsList)
                        .label(message("builder.settings.icon"), LabelPosition.TOP)
                        .enabled(false)
                        .applyToComponent {
                            selectedIndex = -1
                            renderer = listCellRenderer { label ->
                                text = label
                                icon = IconUtils.getIconByValue(label)
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
                        .bindText(itemProperty, Item::filePath)
                        .onChanged {
                            val extension = FileUtils.getExtension(it.text)
                            iconComboBox.component.selectedItem = IconUtils.getIconValueByExtension(
                                extension,
                                itemProperty.get()?.parent?.isNotEmpty() ?: false
                            )
                        }
                }

                row {
                    templateTextArea = cell(BuilderEditor(project))
                        .label(message("builder.settings.template"), LabelPosition.TOP)
                        .bindText(itemProperty, Item::template)
                        .align(AlignX.FILL)
                        .applyToComponent {
                            preferredHeight = JBUI.scale(200)
                        }
                }
            }
        }.enabledIf(selectedRowPredicate)
    }
}


private fun <T : JTextComponent> Cell<T>.bindText(
    graphProperty: ObservableMutableProperty<Item?>,
    property: KMutableProperty1<Item, String>
) =
    bindText(with(graphProperty) {
        transform(
            { it?.let(property::get).orEmpty() },
            { value ->
                get()?.copy()?.apply {
                    property.set(this, value)
                    set(this)
                }
            }
        )
    })

private fun <T : JBCheckBox> Cell<T>.bindSelected(
    graphProperty: ObservableMutableProperty<Item?>,
    property: KMutableProperty1<Item, Boolean>
) =
    bindSelected(with(graphProperty) {
        transform(
            { it?.let(property::get) ?: false },
            { value ->
                get()?.copy()?.apply {
                    property.set(this, value)
                    set(this)
                }
            }
        )
    })
