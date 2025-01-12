package org.joel3112.componentbuilder.components

import com.intellij.application.options.EditorFontsConstants
import com.intellij.lang.Language
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.editor.impl.EditorFactoryImpl
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.joel3112.componentbuilder.utils.preferredHeight
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JTextArea
import javax.swing.ScrollPaneConstants

class BuilderEditor(val project: Project) : JTextArea() {
    val editor: EditorEx = createEditor()

    var language: Language? = null
        set(value) {
            this.setSyntaxHighlighting(value)
        }

    var fontSize: Int = EditorFontsConstants.getDefaultEditorFontSize()
        set(value) {
            field = value
            updateEditorFontSize()
        }

    private fun updateEditorFontSize() {
        editor.colorsScheme = (editor.colorsScheme.clone() as EditorColorsScheme).apply {
            editorFontSize = fontSize // Apply the font size to the cloned scheme
        }
    }

    init {
        layout = BorderLayout()
        add(editor.component)

        editor.component.isFocusable = true
        editor.component.isFocusCycleRoot = true
        addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                editor.contentComponent.requestFocusInWindow()
            }
        })

        fontSize = JBUI.scaleFontSize(12f)
        preferredHeight(JBUI.scale(300))
        minimumSize = preferredSize
    }

    override fun setText(value: String) {
        super.setText(value)
        runWriteAction {
            if (editor.document.text != value) {
                editor.document.setText(value)
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        editor.isViewer = !enabled
        editor.setCaretEnabled(enabled)
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

    private fun setSyntaxHighlighting(language: Language?) {
        if (language != null) {
            // Retrieve the appropriate syntax highlighter for the given language
            val syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(language, project, null)

            if (syntaxHighlighter != null) {
                // Use the EditorHighlighterFactory to apply the highlighter to the editor
                val editorHighlighter = EditorHighlighterFactory.getInstance()
                    .createEditorHighlighter(syntaxHighlighter, editor.colorsScheme)
                editor.highlighter = editorHighlighter
            } else {
                // Fallback to plain text highlighter if no syntax highlighter is found
                val plainTextHighlighter =
                    EditorHighlighterFactory.getInstance().createEditorHighlighter(project, PlainTextFileType.INSTANCE)
                editor.highlighter = plainTextHighlighter
            }
        } else {
            // Handle the case where language is null by applying a plain text highlighter
            val plainTextHighlighter =
                EditorHighlighterFactory.getInstance().createEditorHighlighter(project, PlainTextFileType.INSTANCE)
            editor.highlighter = plainTextHighlighter
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
