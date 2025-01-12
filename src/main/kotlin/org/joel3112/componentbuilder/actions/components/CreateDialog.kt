package org.joel3112.componentbuilder.actions.components

import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.components.BuilderEditor
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.utils.IconUtils
import org.joel3112.componentbuilder.utils.preferredWidth
import javax.swing.JComponent
import javax.swing.SwingUtilities


private val NAME_REGEX = "^[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*$".toRegex()
private fun String.isValidName(): Boolean {
    return this.isNotEmpty() && this.matches(NAME_REGEX)
}

open class CreateDialog(project: Project, val item: Item) : DialogWrapper(project) {

    private val settingsService = project.service<SettingsService>()
    private val children = settingsService.getChildrenByItem(item)

    private lateinit var textField: Cell<JBTextField>
    private lateinit var templateEditor: Cell<BuilderEditor>
    private var commentLabel: Cell<CommentLabel>? = null
    private var childrenCommentLabel: MutableList<Cell<CommentLabel>> = mutableListOf()

    private val hasChildrenPredicate = object : ComponentPredicate() {
        override fun invoke() = children.isNotEmpty()

        override fun addListener(listener: (Boolean) -> Unit) =
            listener(children.isNotEmpty())
    }

    private val isValidNamePredicate = object : ComponentPredicate() {
        override fun invoke() = isValidNameProperty.get()

        override fun addListener(listener: (Boolean) -> Unit) =
            isValidNameProperty.afterChange { listener(it) }
    }

    var isCanceled = false
        protected set


    private val propertyGraph = PropertyGraph()
    private val isValidNameProperty = propertyGraph
        .property(false)
        .apply {
            afterChange { valid -> okAction.isEnabled = valid }
        }
    private val nameProperty = propertyGraph
        .property("")
        .apply {
            afterChange { value ->
                commentLabel!!.component.inputName = value
                childrenCommentLabel.forEach { it.component.inputName = value }
                isValidNameProperty.set(value.isValidName())

                templateProperty.set(
                    if (value.isValidName())
                        item.templateFormatted(value, settingsService.variables)
                    else ""
                )
                templateEditor.component.language = item.language
                templateEditor.component.isEnabled = value.isValidName()
                repaint()
            }
        }
    private val templateProperty = propertyGraph
        .property("")


    val cname: String
        get() = nameProperty.get()
    val ctemplate: String
        get() = templateProperty.get()
    val selectedChildren: MutableList<Item> = mutableListOf()

    private inner class CommentLabel(val item: Item) : JBLabel() {
        var inputName: String = ""
            set(value) {
                field = value

                val commentText = if (inputName.isNotEmpty()) {
                    item.filePathFormatted(inputName, settingsService.variables)
                } else {
                    ""
                }
                text = if (value.isValidName()) "<html><small>${commentText}</small></html>" else ""
                icon = if (value.isValidName()) IconUtils.getIconByItem(item).second else null
            }

        init {
            isOpaque = true
            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        }
    }


    private val createPanel = panel {
        row {
            textField = textField()
                .align(AlignX.FILL)
                .label(message("builder.popup.create.name.label"), LabelPosition.TOP)
                .focused()
                .columns(33)
                .cellValidation {
                    addInputRule(message("builder.popup.create.name.validation.empty")) {
                        it.text.isEmpty()
                    }
                    addInputRule(message("builder.popup.create.name.validation.specialCharacters")) {
                        !it.text.matches(NAME_REGEX)
                    }
                }
                .applyToComponent {
                    document.addDocumentListener(object : DocumentAdapter() {
                        override fun textChanged(e: javax.swing.event.DocumentEvent) {
                            val originalText = text
                            val formattedText = originalText.replace(" ", "-")

                            if (formattedText != originalText) {
                                SwingUtilities.invokeLater {
                                    document.removeDocumentListener(this)
                                    text = formattedText.trim()
                                    document.addDocumentListener(this)
                                }
                            }
                            nameProperty.set(text)
                        }
                    })
                }
        }.bottomGap(BottomGap.NONE)

        row {
            commentLabel = cell(CommentLabel(item)).align(AlignX.FILL)
        }.topGap(TopGap.NONE)

        row {
            templateEditor = cell(BuilderEditor(project))
                .bindText(templateProperty)
                .align(AlignX.FILL)
                .applyToComponent {
                    preferredWidth(JBUI.scale(550))
                }
        }

        collapsibleGroup(message("builder.popup.create.advanced.options.title")) {
            row {
                text(message("builder.popup.create.advanced.options.select.children"))
            }.enabledIf(isValidNamePredicate)

            for (child in children) {
                row {
                    checkBox(child.name)
                        .onChanged {
                            if (it.isSelected) {
                                selectedChildren.add(child)
                            } else {
                                selectedChildren.remove(child)
                            }
                        }

                    childrenCommentLabel.add(
                        cell(CommentLabel(child))
                            .align(AlignX.FILL)
                            .visibleIf(isValidNamePredicate)
                    )
                }
                    .enabledIf(isValidNamePredicate)
                    .bottomGap(BottomGap.NONE).layout(RowLayout.PARENT_GRID)
            }
        }.apply {
            addExpandedListener { repaint() }
        }.visibleIf(hasChildrenPredicate)
    }

    init {
        super.init()
        isResizable = false
        title = message("builder.popup.create.title", item.name)
        okAction.isEnabled = false
    }

    override fun createCenterPanel(): JComponent = createPanel

    override fun doOKAction() {
        isCanceled = false
        super.doOKAction()
        repaint()
    }

    override fun doCancelAction() {
        isCanceled = true
        super.doCancelAction()
    }

    override fun repaint() {
        super.repaint()
        SwingUtilities.invokeLater { setSize(0, 0) }
    }
}
