package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorFactoryImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.JTextArea
import javax.swing.ScrollPaneConstants

class BuilderEditor(val project: Project) : JTextArea() {
    private val editor: EditorEx = createEditor()

    init {
        layout = BorderLayout()
        add(editor.component)
    }

    override fun setText(value: String) {
        super.setText(value)
        runWriteAction {
            if (editor.document.text != value) {
                editor.document.setText(value)
            }
        }
    }

    private inner class TextChangeListener : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            val currentText = event.document.text
            text = currentText
        }
    }

    private fun EditorEx.syncEditorColors() {
        setBackgroundColor(null) // To use background from set color scheme

        val isLaFDark = ColorUtil.isDark(UIUtil.getPanelBackground())
        val isEditorDark = EditorColorsManager.getInstance().isDarkEditor
        colorsScheme = if (isLaFDark == isEditorDark) {
            EditorColorsManager.getInstance().globalScheme
        } else {
            EditorColorsManager.getInstance().schemeForCurrentUITheme
        }
    }

    private fun createEditor(): EditorEx {
        val editorFactory = EditorFactory.getInstance()
        val document = (editorFactory as EditorFactoryImpl).createDocument(text)
        val editor = editorFactory.createEditor(document) as EditorEx

        Disposer.register(project) { EditorFactory.getInstance().releaseEditor(editor) }

        return editor.apply {
            document.addDocumentListener(TextChangeListener())

            setBorder(JBUI.Borders.empty())
            setCaretVisible(true)
            syncEditorColors()
            scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED

            settings.apply {
                isLineMarkerAreaShown = true
                isIndentGuidesShown = true
                isLineNumbersShown = true
                isWhitespacesShown = false
                isFoldingOutlineShown = true
                additionalColumnsCount = 0
                additionalLinesCount = 0
                isRightMarginShown = false
                setTabSize(2)
            }
        }
    }
}
