package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.Graphics
import java.awt.Insets
import javax.swing.Icon

class BuilderIconDescription : JBTextField() {

    var icon: Icon? = null
        set(value) {
            field = value
            revalidate()
            repaint()
        }

    init {
        isEditable = false
        isFocusable = false
    }

    override fun setText(text: String?) {
        super.setText(text)
        revalidate()
        repaint()
    }


    override fun getInsets(): Insets {
        // Get the current insets from the border or default to empty insets
        val currentInsets: Insets = border?.getBorderInsets(this) ?: JBUI.emptyInsets()

        // Calculate the additional insets required for the icon
        val iconWidth = icon?.iconWidth ?: 0
        val iconInsets = if (iconWidth > 0) iconWidth else 0

        // Add the icon insets to the left side of the current insets
        return JBUI.insets(
            currentInsets.top,
            currentInsets.left + iconInsets + 2,
            currentInsets.bottom,
            currentInsets.right
        )
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        icon?.let {
            // Calculate icon position considering the border insets
            val iconX = (border?.getBorderInsets(this)?.left ?: 0) + 4
            val iconY = (height - it.iconHeight) / 2 // Center the icon vertically

            // Paint the icon
            it.paintIcon(this, g, iconX, iconY)
        }
    }
}
