package org.joel3112.componentbuilder.settings.ui.components

import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Insets
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.UIManager


class BuilderIconDescription : JComponent() {

    private val iconLabel: JLabel = JLabel()
    private val textLabel: JLabel = JLabel()
    private var iconSpacing: Int = JBUI.scale(2)
    private var sidePadding: Int = JBUI.scale(4)

    private var customPreferredSize: Dimension? = null
    private val minimumHeight = UIManager.getDimension("TextField.minimumSize").height

    init {
        layout = null
        add(iconLabel)
        add(textLabel)
        border = UIManager.getBorder("TextField.border")
        isOpaque = true
    }

    var icon: Icon?
        get() = iconLabel.icon
        set(value) {
            iconLabel.icon = value
            revalidate()
            repaint()
        }

    var text: String?
        get() = textLabel.text
        set(value) {
            textLabel.text = value
            revalidate()
            repaint()
        }

    override fun doLayout() {
        val iconPreferredSize = iconLabel.preferredSize
        val textPreferredSize = textLabel.preferredSize

        // Get the border insets
        val insets: Insets = border?.getBorderInsets(this) ?: JBUI.emptyInsets()

        // Calculate the available width and height for content
        val availableWidth = width - insets.left - insets.right
        val availableHeight = height - insets.top - insets.bottom

        // Layout the icon on the left with left padding and border insets
        val iconX = insets.left + sidePadding
        val iconY = insets.top + (availableHeight - iconPreferredSize.height) / 2
        iconLabel.setBounds(
            iconX,
            iconY,
            iconPreferredSize.width,
            iconPreferredSize.height
        )

        // Layout the text to the right of the icon with spacing, padding, and border insets
        val textX = iconX + iconPreferredSize.width + iconSpacing
        val textY = insets.top + (availableHeight - textPreferredSize.height) / 2
        textLabel.setBounds(
            textX,
            textY,
            textPreferredSize.width,
            textPreferredSize.height
        )
    }

    override fun getPreferredSize(): Dimension {
        return customPreferredSize?.let {
            val preferredHeight = if (it.height == 0) minimumHeight else it.height

            // Adjust preferred height by including insets
            val insets: Insets = border?.getBorderInsets(this) ?: JBUI.emptyInsets()
            val totalHeight = preferredHeight + insets.top + insets.bottom

            Dimension(it.width, totalHeight)
        } ?: calculatePreferredSize()
    }

    override fun setPreferredSize(preferredSize: Dimension?) {
        customPreferredSize = preferredSize
        revalidate()
        repaint()
    }

    private fun calculatePreferredSize(): Dimension {
        val iconPreferredSize = iconLabel.preferredSize
        val textPreferredSize = textLabel.preferredSize

        // Get the border insets
        val insets: Insets = border?.getBorderInsets(this) ?: JBUI.emptyInsets()

        // Calculate the width based on content
        val calculatedWidth = insets.left + sidePadding + iconPreferredSize.width + iconSpacing + textPreferredSize.width + sidePadding + insets.right

        // Ensure minimum height is the default TextField minimum height, including insets
        val calculatedHeight = maxOf(minimumHeight, iconPreferredSize.height + insets.top + insets.bottom)

        return Dimension(calculatedWidth, calculatedHeight)
    }

    override fun getMinimumSize(): Dimension {
        val insets: Insets = border?.getBorderInsets(this) ?: JBUI.emptyInsets()

        // Ensure minimum height includes insets
        val totalHeight = minimumHeight + insets.top + insets.bottom
        return Dimension(preferredSize.width, totalHeight)
    }

    override fun paintComponent(g: Graphics) {
        g.color = background
        g.fillRect(0, 0, width, height)

        super.paintComponent(g)
    }
}
