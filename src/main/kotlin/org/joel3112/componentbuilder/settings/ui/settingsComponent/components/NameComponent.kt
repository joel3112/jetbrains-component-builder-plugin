package org.joel3112.componentbuilder.settings.ui.settingsComponent.components

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import org.joel3112.componentbuilder.settings.data.MutableState
import org.joel3112.componentbuilder.settings.ui.settingsComponent.Component
import javax.swing.JLabel
import javax.swing.JPanel

class NameComponent(name: String?) : Component, MutableState<String> {
    private val textField = JBTextField(name)

    override fun addToBuilder(formBuilder: FormBuilder) {
        val panel = JPanel()
        panel.add(JLabel("Name:"))
        panel.add(textField)
        formBuilder.addLabeledComponent(panel, JLabel())
    }

    override var state: String
        get() = textField.text
        set(state) {
            textField.text = state
        }
}
