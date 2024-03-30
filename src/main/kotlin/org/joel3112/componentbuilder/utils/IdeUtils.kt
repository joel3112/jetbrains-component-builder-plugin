package org.joel3112.componentbuilder.utils

import com.intellij.application.options.colors.ColorAndFontOptions
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.Disposer

object IdeUtils {
    fun createEditorPreview(text: String?, editable: Boolean, disposable: Disposable?): EditorImpl {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val options = ColorAndFontOptions()
        options.reset()
        options.selectScheme(scheme.name)
        val editorFactory = EditorFactory.getInstance()
        val editorDocument = editorFactory.createDocument(text!!)
        val editor =
            (if (editable) editorFactory.createEditor(editorDocument) else editorFactory.createViewer(editorDocument)) as EditorEx
        editor.colorsScheme = scheme
        val settings = editor.settings
        settings.isLineNumbersShown = true
        settings.isWhitespacesShown = false
        settings.isLineMarkerAreaShown = true
        settings.isIndentGuidesShown = true
        settings.isFoldingOutlineShown = true
        settings.additionalColumnsCount = 0
        settings.additionalLinesCount = 0
        settings.isRightMarginShown = false

        Disposer.register(disposable!!) {
            EditorFactory.getInstance().releaseEditor(editor)
        }
        return editor as EditorImpl
    }
}
