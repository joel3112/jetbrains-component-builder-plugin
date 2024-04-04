package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.icons.AllIcons.FileTypes
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
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty1


class BuilderItemsEditor(val itemProperty: ObservableMutableProperty<Item?>, project: Project) :
    UiDslUnnamedConfigurable.Simple() {

    private lateinit var isChildFileCheckBox: Cell<JBCheckBox>
    private lateinit var nameTextField: Cell<JBTextField>
    private lateinit var iconComboBox: Cell<ComboBox<String>>
    private lateinit var filePathTextField: Cell<JBTextField>
    private val templateEditor = BuilderEditor(project)

    private val allIconsList = FileTypes::class.java.fields.map { it.name }

    private val selectedRowPredicate = object : ComponentPredicate() {
        override fun invoke() = itemProperty.isNotNull().get()

        override fun addListener(listener: (Boolean) -> Unit) =
            itemProperty.afterChange {
                listener(it != null)
            }
    }

    init {
        itemProperty.afterChange {
            if (it != null && it.icon.isEmpty()) {
                iconComboBox.component.selectedIndex = -1
            }
        }
    }

    override fun Panel.createContent() {
        panel {
            row {
                isChildFileCheckBox = checkBox(message("builder.settings.isChildFile"))
                    .comment(message("builder.settings.isChildFile.legend"), 50)

                    .bindSelected(
                        itemProperty, Item::isChildFile
                    )
            }.bottomGap(BottomGap.NONE)

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
                        .bindItem(itemProperty, Item::icon)
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
                        .bindText(itemProperty, Item::filePath)
                }

                row {
                    cell(templateEditor)
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
            { value -> get()?.apply { property.set(this, value) } },
        )
    })

private fun <T : JBCheckBox> Cell<T>.bindSelected(
    graphProperty: ObservableMutableProperty<Item?>,
    property: KMutableProperty1<Item, Boolean>
) =
    bindSelected(with(graphProperty) {
        transform(
            { it?.let(property::get) ?: false },
            { value -> get()?.apply { property.set(this, value) } },
        )
    })

private fun <T : ComboBox<String>> Cell<T>.bindItem(
    graphProperty: ObservableMutableProperty<Item?>,
    property: KMutableProperty1<Item, String>
) =
    bindItem(with(graphProperty) {
        transform(
            { it?.let(property::get) ?: "" },
            { value -> get()?.apply { property.set(this, value) } },
        )
    })



