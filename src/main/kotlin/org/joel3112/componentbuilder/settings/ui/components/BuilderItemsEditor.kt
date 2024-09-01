package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.utils.IconUtils
import org.joel3112.componentbuilder.utils.bindText
import org.joel3112.componentbuilder.utils.preferredHeight
import org.joel3112.componentbuilder.utils.preferredWidth

class BuilderItemsEditor(
    val itemProperty: GraphProperty<Item?>,
    val project: Project
) :
    UiDslUnnamedConfigurable.Simple() {

    private lateinit var isChildFileCheckBox: Cell<JBCheckBox>
    private lateinit var parentExtensionsTextField: Cell<JBTextField>
    private lateinit var nameTextField: Cell<JBTextField>
    private lateinit var iconFileDescription: Cell<BuilderIconDescription>
    private lateinit var filePathTextField: Cell<JBTextField>
    private lateinit var templateEditor: Cell<BuilderEditor>

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

    init {
        itemProperty.afterChange {
            if (it != null) {
                val (fileType, icon) = IconUtils.getIconByItem(it)
                iconFileDescription.component.icon = icon!!
                iconFileDescription.component.text = fileType
            }
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

                    iconFileDescription = cell(BuilderIconDescription())
                        .label(message("builder.settings.icon"), LabelPosition.TOP)
                        .visibleIf(selectedRowPredicate)
                        .applyToComponent {
                            preferredWidth(JBUI.scale(150))
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
                    templateEditor = cell(BuilderEditor(project))
                        .label(message("builder.settings.template"), LabelPosition.TOP)
                        .bindText(itemProperty, Item::template)
                        .align(AlignX.FILL)
                        .applyToComponent {
                            preferredHeight(JBUI.scale(200))
                        }
                }
            }
        }.enabledIf(selectedRowPredicate)
    }
}

