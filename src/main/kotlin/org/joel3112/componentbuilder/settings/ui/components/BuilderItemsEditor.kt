package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.codeInsight.template.impl.TemplateImplUtil
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
import org.joel3112.componentbuilder.components.BuilderEditor
import org.joel3112.componentbuilder.components.BuilderLabeledIcon
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.utils.IconUtils
import org.joel3112.componentbuilder.utils.preferredHeight
import org.joel3112.componentbuilder.utils.preferredWidth
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty1

class BuilderItemsEditor(
    val settingsProperty: GraphProperty<SettingsService>,
    val itemProperty: GraphProperty<Item?>,
    val project: Project
) :
    UiDslUnnamedConfigurable.Simple() {

    private lateinit var isChildFileCheckBox: Cell<JBCheckBox>
    private lateinit var parentExtensionsTextField: Cell<JBTextField>
    private lateinit var nameTextField: Cell<JBTextField>
    private lateinit var iconFileDescription: Cell<BuilderLabeledIcon>
    private lateinit var filePathTextField: Cell<JBTextField>
    private lateinit var matchRegexTextField: Cell<JBTextField>
    private lateinit var templateEditor: Cell<BuilderEditor>

    val templateVariables
        get() = TemplateImplUtil.parseVariables(
            "${itemProperty.get()?.filePath} - ${itemProperty.get()?.template}"
        ).map { it.value.name }


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

    private val hasTemplatePredicate = object : ComponentPredicate() {
        override fun invoke() = templateVariables.isNotEmpty()

        override fun addListener(listener: (Boolean) -> Unit) =
            itemProperty.afterChange {
                listener(templateVariables.isNotEmpty())
            }
    }

    init {
        itemProperty.afterChange {
            if (it != null) {
                val (fileType, icon) = IconUtils.getIconByItem(it)
                iconFileDescription.component.icon = icon
                iconFileDescription.component.text = fileType
                templateEditor.component.language = it.language
            }
        }
    }


    override fun Panel.createContent() {
        panel {
            row {
                panel {
                    row {
                        text(message("builder.settings.parent.legend"), 60)
                    }
                    row {
                        matchRegexTextField = expandableTextField()
                            .label(message("builder.settings.regexMatch"), LabelPosition.LEFT)
                            .columns(COLUMNS_MEDIUM)
                            .bindText(itemProperty, Item::regexMatch)

                    }.rowComment(message("builder.settings.regexMatch.legend"), 60)
                }
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

                    iconFileDescription = cell(BuilderLabeledIcon())
                        .label(message("builder.settings.icon"), LabelPosition.TOP)
                        .visibleIf(selectedRowPredicate)
                        .applyToComponent {
                            preferredWidth(JBUI.scale(150))
                        }
                }
            }

            group(message("builder.settings.group.file")) {
                row {
                    comment(message("builder.settings.group.file.variables.description"), 43).gap(RightGap.SMALL)
                    button(message("builder.dialog.variables.button.open")) {
                        val editVariableDialog = VariablesDialog(settingsProperty, templateVariables, project)
                        editVariableDialog.show()

                    }
                        .enabledIf(hasTemplatePredicate)
                        .applyToComponent {
                            isDefaultCapable = false
                            setMaximumSize(getPreferredSize())
                        }
                }

                row {
                    filePathTextField = expandableTextField()
                        .label(message("builder.settings.filePath"), LabelPosition.TOP)
                        .columns(COLUMNS_LARGE)
                        .bindText(itemProperty, Item::filePath)

                    comment(message("builder.settings.filePath.example"))
                }
                    .rowComment(message("builder.settings.filePath.legend"), 60)

                row {
                    templateEditor = cell(BuilderEditor(project))
                        .label(message("builder.settings.template"), LabelPosition.TOP)
                        .bindText(itemProperty, Item::template)
                        .align(AlignX.FILL)
                        .applyToComponent {
                            preferredHeight(JBUI.scale(200))
                            fontSize = JBUI.scaleFontSize(12f)
                        }
                }.topGap(TopGap.SMALL)
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


