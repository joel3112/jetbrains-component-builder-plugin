package org.joel3112.componentbuilder.utils

import com.intellij.util.ui.JBDimension
import javax.swing.JComponent

fun JComponent.preferredWidth(width: Int) {
    preferredSize =
        JBDimension(width, preferredSize?.height ?: minimumSize.height)
}

fun JComponent.preferredHeight(height: Int) {
    preferredSize =
        JBDimension(preferredSize?.width ?: minimumSize.width, height)
}
