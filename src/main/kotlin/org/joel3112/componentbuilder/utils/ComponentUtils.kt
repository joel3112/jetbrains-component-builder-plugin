package org.joel3112.componentbuilder.utils

import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.util.ui.JBDimension
import org.joel3112.componentbuilder.settings.data.Item
import javax.swing.JComponent
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty1

fun JComponent.preferredWidth(width: Int) {
    preferredSize =
        JBDimension(width, preferredSize?.height ?: minimumSize.height)
}

fun JComponent.preferredHeight(height: Int) {
    preferredSize =
        JBDimension(preferredSize?.width ?: minimumSize.width, height)
}

fun <T : JTextComponent> Cell<T>.bindText(
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

fun <T : JBCheckBox> Cell<T>.bindSelected(
    graphProperty: ObservableMutableProperty<Item?>,
    property: KMutableProperty1<Item, Boolean>
) =
    bindSelected(with(graphProperty) {
        transform(
            { it?.let(property::get) ?: false },
            { value ->
                get()?.copy()?.apply {
                    property.set(this, value)
                    set(this)
                }
            }
        )
    })

fun <T : ComboBox<String>> Cell<T>.bindItem(
    graphProperty: ObservableMutableProperty<Item?>,
    property: KMutableProperty1<Item, String>
) =
    bindItem(with(graphProperty) {
        transform(
            { it?.let(property::get) ?: "" },
            { value ->
                get()?.copy()?.apply {
                    property.set(this, value)
                    set(this)
                }
            }
        )
    })

