package org.joel3112.componentbuilder

import com.intellij.application.options.colors.ColorAndFontOptions
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.util.Disposer
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindText
import org.joel3112.componentbuilder.settings.data.Item
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty1

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
    settings.isLineNumbersShown = false
    settings.isWhitespacesShown = false
    settings.isLineMarkerAreaShown = false
    settings.isIndentGuidesShown = false
    settings.isFoldingOutlineShown = false
    settings.additionalColumnsCount = 0
    settings.additionalLinesCount = 0
    settings.isRightMarginShown = false

    Disposer.register(disposable!!) {
        EditorFactory.getInstance().releaseEditor(editor)
    }
    return editor as EditorImpl
}

fun <T : JTextComponent> Cell<T>.bindText(
    graphProperty: ObservableMutableProperty<Item?>,
    property: KMutableProperty1<Item, String>
) =
    bindText(with(graphProperty) {
        transform(
            { it?.let(property::get).orEmpty() },
            { value -> get()?.apply { property.set(this, value) } },
        )
    })
