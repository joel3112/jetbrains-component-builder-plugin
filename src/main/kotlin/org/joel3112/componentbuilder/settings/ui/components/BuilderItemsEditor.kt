package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import com.intellij.ui.layout.ComponentPredicate
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import java.awt.Dimension
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty1


class BuilderItemsEditor(val itemProperty: ObservableMutableProperty<Item?>, project: Project) :
    UiDslUnnamedConfigurable.Simple() {
    private var state = project.service<SettingsService>()

    private lateinit var nameTextField: Cell<JBTextField>
    private lateinit var filePathTextField: Cell<JBTextField>
    private lateinit var templateTextarea: Cell<JBTextArea>
    private lateinit var isChildFileCheckBox: Cell<JBCheckBox>

    private val selectedRowPredicate = object : ComponentPredicate() {
        override fun invoke() = itemProperty.isNotNull().get()

        override fun addListener(listener: (Boolean) -> Unit) =
            itemProperty.afterChange {
                listener(it != null)
            }
    }
    private val checkedIsDependentFilePredicate = object : ComponentPredicate() {
        override fun invoke() = isChildFileCheckBox.component.isSelected

        override fun addListener(listener: (Boolean) -> Unit) =
            itemProperty.afterChange {
                listener((it?.isChildFile == false))
            }
    }


    override fun Panel.createContent() {
        panel {
            row {
                isChildFileCheckBox = checkBox(message("builder.settings.isChildFile"))
                    .bindSelected(
                        itemProperty, Item::isChildFile
                    )
            }

            row(message("builder.settings.name")) {
                nameTextField = textField()
                    .comment(message("builder.settings.name.legend"))
                    .bindText(itemProperty, Item::name)
            }.enabledIf(selectedRowPredicate)

            row(message("builder.settings.filePath")) {
                filePathTextField = expandableTextField()
                    .comment(message("builder.settings.filePath.legend"))
                    .bindText(itemProperty, Item::filePath)
                    .applyToComponent {
                        columns = 35
                    }
            }

            row(message("builder.settings.template")) {}
            row {
                templateTextarea = textArea()
                    .align(Align.FILL)
                    .verticalAlign(VerticalAlign.FILL)
                    .comment(message("builder.settings.template.legend"))
                    .applyToComponent {
                        preferredSize = Dimension(0, 200)
                        font = EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.CONSOLE_PLAIN)
                    }
                    .bindText(itemProperty, Item::template)
            }.resizableRow()
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



