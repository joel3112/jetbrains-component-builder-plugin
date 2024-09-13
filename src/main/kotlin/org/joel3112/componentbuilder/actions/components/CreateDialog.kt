package org.joel3112.componentbuilder.actions.components

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.utils.IconUtils
import javax.swing.JComponent
import javax.swing.SwingUtilities


open class CreateDialog(project: Project, val item: Item) : DialogWrapper(project) {

    private val settingsService = project.service<SettingsService>()
    private val children = settingsService.getChildrenByItem(item)

    private lateinit var textField: Cell<JBTextField>
    private var commentLabel: Cell<CommentLabel>? = null
    private var childrenCommentLabel: MutableList<Cell<CommentLabel>> = mutableListOf()

    private val hasChildrenPredicate = object : ComponentPredicate() {
        override fun invoke() = children.isNotEmpty()

        override fun addListener(listener: (Boolean) -> Unit) =
            listener(children.isNotEmpty())
    }

    var isCanceled = false
        protected set

    var cname: String = ""
        set(value) {
            field = value

            if (commentLabel != null) {
                commentLabel!!.component.inputName = value
                childrenCommentLabel.forEach { it.component.inputName = value }
            }
        }

    private val isValidName: Boolean
        get() = cname.isNotEmpty() && cname.matches(nameRegex)

    val selectedChildren: MutableList<Item> = mutableListOf()
    private val nameRegex = "^[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*$".toRegex()

    private inner class CommentLabel(val item: Item) : JBLabel() {
        var inputName: String = ""
            set(value) {
                field = value

                val commentText = if (inputName.isNotEmpty()) {
                    item.filePathFormatted(inputName)
                } else {
                    ""
                }
                text = "<html><small>${commentText}</small></html>"
                icon = if (isValidName) IconUtils.getIconByItem(item).second else null
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
                .validationInfo {
                    if (it.text.isEmpty()) {
                        ValidationInfo(message("builder.popup.create.name.validation.empty"))
                    } else if (!it.text.matches(nameRegex)) {
                        ValidationInfo(message("builder.popup.create.name.validation.specialCharacters"))
                    } else {
                        null
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
                            cname = text
                            okAction.isEnabled = isValidName
                        }
                    })
                }
        }.bottomGap(BottomGap.NONE)

        row {
            commentLabel = cell(CommentLabel(item)).align(AlignX.FILL)
        }.topGap(TopGap.NONE)

        collapsibleGroup(message("builder.popup.create.advanced.options.title")) {
            row {
                text(message("builder.popup.create.advanced.options.select.children"))
            }
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

                    childrenCommentLabel.add(cell(CommentLabel(child)).align(AlignX.FILL))
                }.bottomGap(BottomGap.NONE).layout(RowLayout.PARENT_GRID)
            }
        }.apply {
            addExpandedListener {
                SwingUtilities.invokeLater { setDimensions() }
            }
        }.visibleIf(hasChildrenPredicate)
    }

    init {
        super.init()
        isResizable = false
        title = message("builder.popup.create.title", item.name)
        okAction.isEnabled = false
        setDimensions()
    }

    override fun createCenterPanel(): JComponent = createPanel

    override fun doOKAction() {
        isCanceled = false
        super.doOKAction()
    }

    override fun doCancelAction() {
        isCanceled = true
        super.doCancelAction()
    }

    private fun setDimensions() {
        setSize(JBUI.scale(360), 0)
    }
}
