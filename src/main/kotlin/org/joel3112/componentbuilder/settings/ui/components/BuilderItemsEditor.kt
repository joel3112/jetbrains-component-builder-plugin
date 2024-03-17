package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import com.intellij.ui.layout.ComponentPredicate
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.bindText
import org.joel3112.componentbuilder.settings.data.Item
import java.awt.Dimension


class BuilderItemsEditor(val itemProperty: ObservableMutableProperty<Item?>, project: Project) :
    UiDslUnnamedConfigurable.Simple() {
    private lateinit var nameTextField: Cell<JBTextField>
    private lateinit var filePathTextField: Cell<JBTextField>
    private lateinit var templateTextarea: Cell<JBTextArea>

    private val selectedRowPredicate = object : ComponentPredicate() {

        override fun invoke() = itemProperty.isNotNull().get()

        override fun addListener(listener: (Boolean) -> Unit) =
            itemProperty.afterChange {
                listener(it != null)
            }
    }


    override fun Panel.createContent() {
        panel {
            row(message("builder.settings.name")) {
                nameTextField = textField()
                    .comment(message("builder.settings.name.legend"))
                    .bindText(itemProperty, Item::name)
            }.enabledIf(selectedRowPredicate)

            row(message("builder.settings.filePath")) {
                filePathTextField = textField()
                    .comment(message("builder.settings.filePath.legend"))
                    .bindText(itemProperty, Item::filePath)
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
        }
            .enabledIf(selectedRowPredicate)
    }

}


