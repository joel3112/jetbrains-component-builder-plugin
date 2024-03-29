package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.icons.AllIcons.FileTypes
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.util.preferredHeight
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty1


class BuilderItemsEditor(val itemProperty: ObservableMutableProperty<Item?>) :
    UiDslUnnamedConfigurable.Simple() {

    private lateinit var isChildFileCheckBox: Cell<JBCheckBox>
    private lateinit var nameTextField: Cell<JBTextField>
    private lateinit var iconComboBox: Cell<ComboBox<String>>
    private lateinit var filePathTextField: Cell<JBTextField>
    private lateinit var templateTextarea: Cell<JBTextArea>

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
                    .bindSelected(
                        itemProperty, Item::isChildFile
                    )
            }.bottomGap(BottomGap.NONE)

            group(message("builder.settings.group.display")) {
                row {
                    comment(message("builder.settings.group.display.description"))
                }

                row(message("builder.settings.name")) {
                    nameTextField = textField()
                        .bindText(itemProperty, Item::name)
                }

                row(message("builder.settings.icon")) {
                    iconComboBox = comboBox(allIconsList).applyToComponent {
                        selectedIndex = -1
                        renderer = listCellRenderer { label ->
                            text = label
                            icon = FileTypes::class.java.getField(label).get(null) as javax.swing.Icon
                        }
                    }.bindItem(itemProperty, Item::icon)
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

                row(message("builder.settings.template")) {}
                row {
                    templateTextarea = textArea()
                        .align(Align.FILL)
                        .comment(message("builder.settings.template.legend"))
                        .applyToComponent {
                            preferredHeight = JBUI.scale(200)
                            font = EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.CONSOLE_PLAIN)
                        }
                        .resizableColumn()
                        .bindText(itemProperty, Item::template)
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



