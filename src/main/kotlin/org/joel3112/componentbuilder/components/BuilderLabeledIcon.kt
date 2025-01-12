package org.joel3112.componentbuilder.components

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.UIManager
import javax.swing.border.Border

class BuilderLabeledIcon : JComponent() {

    var text: String? = null
    var icon: Icon? = null

    init {
        // Propiedades visuales
        isOpaque = true
        background = JBColor(UIManager.getColor("TextField.background"), UIManager.getColor("TextField.background"))
        foreground = JBColor(UIManager.getColor("TextField.foreground"), UIManager.getColor("TextField.foreground"))

        // Borde idéntico al de JBTextField
        border = createJBTextFieldBorder()

        // Dimensiones estándar de JBTextField
        minimumSize = JBUI.size(100, 24)
        preferredSize = JBUI.size(200, 30)
    }

    private fun createJBTextFieldBorder(): Border {
        return UIManager.getBorder("TextField.border") ?: JBUI.Borders.customLine(JBColor.border(), 1)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        // Dibujar manualmente el texto y el ícono con padding
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)


        // Calcular el área disponible
        val paddingSize = if (icon != null) JBUI.scale(9) else JBUI.scale(5)
        val contentHeight = height
        val iconWidth = icon?.iconWidth ?: 0
        val iconHeight = icon?.iconHeight ?: 0

        // Calcular posición del ícono
        val iconX = paddingSize
        val iconY = (contentHeight - iconHeight) / 2

        // Dibujar ícono
        icon?.paintIcon(this, g2d, iconX, iconY)

        // Calcular posición del texto
        val textX = iconX + iconWidth + JBUI.scale(4) // Espaciado entre ícono y texto
        val textY = (contentHeight + g2d.fontMetrics.ascent - g2d.fontMetrics.descent) / 2

        // Dibujar texto
        g2d.color = foreground
        g2d.drawString(text ?: "", textX, textY)
    }
}
