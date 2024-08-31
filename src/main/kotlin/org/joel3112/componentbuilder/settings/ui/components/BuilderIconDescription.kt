package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.icons.ExpUiIcons
import com.intellij.ui.components.JBTextField
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.Icon

class BuilderIconDescription : JBTextField() {
    private var iconSpacing: Int = 0
    var icon: Icon = ExpUiIcons.FileTypes.AnyType
        set(value) {
            field = value
            revalidate()  // Recalculate layout and size
            repaint()     // Trigger repaint
        }

    init {
        border = null
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g.create() as Graphics2D
        g2d.paint = background
        g2d.fillRect(0, 0, width, height)

        // Draw the icon
        val iconWidth = icon.iconWidth
        val iconHeight = icon.iconHeight
        val iconX = insets.left
        val iconY = (height - iconHeight) / 2

        icon.paintIcon(this, g2d, iconX, iconY)

        // Translate graphics context to position text correctly
        g2d.translate(iconX + iconWidth + iconSpacing, 0)

        // Draw the text
        super.paintComponent(g2d)
        g2d.dispose()
    }

    override fun getPreferredSize(): Dimension {
        return calculatePreferredSize()
    }

    override fun getMinimumSize(): Dimension {
        return calculatePreferredSize()
    }

    override fun getMaximumSize(): Dimension {
        return calculatePreferredSize()
    }

    private fun calculatePreferredSize(): Dimension {
        val textFieldSize = super.getPreferredSize()
        val iconWidth = icon.iconWidth
        val iconHeight = icon.iconHeight
        val extraWidth = iconWidth + iconSpacing
        val width = Math.max(textFieldSize.width, extraWidth)
        val height = Math.max(textFieldSize.height, iconHeight)
        return Dimension(width + extraWidth, height)
    }

    override fun setText(text: String?) {
        super.setText(text)
        revalidate()
    }
}
