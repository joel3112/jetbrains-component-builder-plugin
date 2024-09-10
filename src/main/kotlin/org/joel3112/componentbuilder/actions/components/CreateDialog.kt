package org.joel3112.componentbuilder.actions.components

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import javax.swing.JComponent
import javax.swing.SwingUtilities


open class CreateDialog(project: Project, val item: Item) : DialogWrapper(project) {
    var isCanceled = false
        protected set

    val cname: String
        get() = textField.component.text

    private val nameRegex = "^[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*$".toRegex()
    private lateinit var textField: Cell<JBTextField>

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
                            val originalText = text // The text entered by the user
                            val formattedText = originalText.replace(" ", "-")

                            if (formattedText != originalText) {
                                SwingUtilities.invokeLater {
                                    document.removeDocumentListener(this)
                                    text = formattedText.trim()
                                    document.addDocumentListener(this)
                                }
                            }

                            okAction.isEnabled = text.isNotEmpty() && text.matches(nameRegex)
                        }
                    })

                }

        }
    }

    init {
        super.init()
        isResizable = false
        title = message("builder.popup.create.title", item.name)
        okAction.isEnabled = false
        setSize(JBUI.scale(360), 0)
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
}
