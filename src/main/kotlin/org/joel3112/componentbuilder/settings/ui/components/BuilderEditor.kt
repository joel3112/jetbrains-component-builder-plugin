package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.project.Project
import org.joel3112.componentbuilder.utils.IdeUtils
import java.awt.BorderLayout
import javax.swing.JTextArea

class BuilderEditor(project: Project) : JTextArea() {
    private var editorComponent = EditorComponentImpl(IdeUtils.createEditorPreview("", true, project))

    init {
        layout = BorderLayout()
        add(editorComponent.editor.component)
        editorComponent.editor.document.addDocumentListener(object :
            com.intellij.openapi.editor.event.DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                setText(event.document.text)
            }
        })
    }

    override fun getText(): String {
        return editorComponent.getText()
    }

    override fun setText(value: String) {
        editorComponent.text = value
        super.setText(value)
    }
}
