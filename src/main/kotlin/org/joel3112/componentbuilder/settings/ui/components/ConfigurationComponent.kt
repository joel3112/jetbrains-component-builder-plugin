package org.joel3112.componentbuilder.settings.ui.components//package org.joel3112.componentbuilder.settings.ui.settingsComponent
//
//import com.intellij.ui.DocumentAdapter
//import com.intellij.ui.components.JBTextField
//import com.intellij.util.ui.FormBuilder
//import org.joel3112.componentbuilder.settings.data.Item
//import org.joel3112.componentbuilder.settings.data.MutableState
//import javax.swing.JLabel
//import javax.swing.JPanel
//
//class ConfigurationComponent(private var item: Item) : Component, MutableState<Item> {
//    private val nameTextField = JBTextField()
//
//    fun addNameChangeListener(listener: (Item, String) -> Unit) {
//        nameTextField.document.addDocumentListener(object : DocumentAdapter() {
//            override fun textChanged(e: javax.swing.event.DocumentEvent) {
//                if (e.type == javax.swing.event.DocumentEvent.EventType.INSERT) {
//                    listener(state, nameTextField.text)
//                }
//            }
//        })
//    }
//
//    override fun addToBuilder(formBuilder: FormBuilder) {
//        val panel = JPanel()
//        panel.add(JLabel("Name:"))
//        panel.add(nameTextField)
//        formBuilder.addLabeledComponent(panel, JLabel())
//    }
//
//    override var state: Item
//        get() = Item(this.item.id, nameTextField.text)
//        set(value) {
//            this.item = value
//            nameTextField.text = value.name
//        }
//}
