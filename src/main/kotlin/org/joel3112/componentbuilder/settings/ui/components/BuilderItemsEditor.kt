package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.not
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.utils.IconUtils
import org.joel3112.componentbuilder.utils.preferredHeight
import org.joel3112.componentbuilder.utils.preferredWidth
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
                text(message("builder.settings.parent.legend"), 60)
            }.visibleIf(isChildFilePredicate.not()).bottomGap(BottomGap.NONE)
            row {
                text(message("builder.settings.child.legend"), 60)
            }.visibleIf(isChildFilePredicate).bottomGap(BottomGap.NONE)

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

                row {
                    panel {
                        row {
                            comment(message("builder.settings.regexPath.legend"), 60)
                        }

                row(message("builder.settings.regexPath")) {
                    filePathTextField = expandableTextField()
                        .columns(COLUMNS_LARGE)
                                .comment(message("builder.settings.regexPath.example"))
                        .bindText(itemProperty, Item::filePath)
                }
                    }
                }.visibleIf(isChildFilePredicate.not())

                row {
                    panel {
                        row {
                            comment(message("builder.settings.filePath.legend"), 60)
                        }

                row(message("builder.settings.filePath")) {
                    filePathTextField = expandableTextField()
                        .columns(COLUMNS_LARGE)
                                .comment(message("builder.settings.filePath.example"))
                        .bindText(itemProperty, Item::filePath)
                }
                    }
                }.visibleIf(isChildFilePredicate)

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


