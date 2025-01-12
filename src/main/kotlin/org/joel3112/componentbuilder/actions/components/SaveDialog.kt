package org.joel3112.componentbuilder.actions.components

import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.not
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.components.BuilderEditor
import org.joel3112.componentbuilder.components.BuilderLabeledIcon
import org.joel3112.componentbuilder.settings.data.Item
import org.joel3112.componentbuilder.utils.IconUtils
import org.joel3112.componentbuilder.utils.preferredWidth
import javax.swing.JComponent
import javax.swing.ListCellRenderer


open class SaveDialog(
    project: Project,
    selectedTemplate: String,
    val item: Item?,
    private val itemsSameExtension: List<Item>
) : DialogWrapper(project) {

    private lateinit var templateEditor: Cell<BuilderEditor>
    private val propertyGraph = PropertyGraph()
    private val templateProperty = propertyGraph
        .property("")
    private val selectedItemProperty = propertyGraph
        .property<Item?>(null)

    private val matchByRegexPredicate = object : ComponentPredicate() {
        override fun invoke() = item != null

        override fun addListener(listener: (Boolean) -> Unit) =
            listener(item != null)
    }

    var isCanceled = false
        protected set
    val ctemplate: String
        get() = templateProperty.get()
    val cselectedItem: Item?
        get() = selectedItemProperty.get()

    private val savePanel = panel {
        row {
            cell(BuilderLabeledIcon().apply {
                this.text = item?.name ?: ""
                this.icon = if (item != null) IconUtils.getIconByItem(item).second else null
            })
                .label(message("builder.popup.save.name.label.matched"), LabelPosition.TOP)
                .align(AlignX.FILL)
        }.visibleIf(matchByRegexPredicate)

        row {
            val listRenderer =
                ListCellRenderer<Item?> { list, value, _, isSelected, _ ->
                    val label = JBLabel(value.name, IconUtils.getIconByItem(value).second, JBLabel.LEFT)

                    if (isSelected) {
                        label.background = list?.selectionBackground
                    } else {
                        label.background = null
                        label.foreground = list?.foreground
                    }
                    label.isOpaque = true
                    label
                }

            comboBox(itemsSameExtension, listRenderer)
                .label(message("builder.popup.save.name.label"), LabelPosition.TOP)
                .comment(message("builder.popup.save.name.description"))
                .align(AlignX.FILL)
                .onChanged {
                    selectedItemProperty.set(it.item)
                }
        }.visibleIf(matchByRegexPredicate.not())

        row {
            templateEditor = cell(BuilderEditor(project))
                .bindText(templateProperty)
                .focused()
                .label(message("builder.settings.template"), LabelPosition.TOP)
                .align(Align.FILL)
                .applyToComponent {
                    preferredWidth(JBUI.scale(600))
                }
        }.resizableRow().topGap(TopGap.SMALL)
    }

    init {
        super.init()
        isResizable = true
        title = message("builder.popup.save.title")
        okAction.isEnabled = true

        templateEditor.component.language = item?.language
        templateProperty.set(selectedTemplate)

        if (item != null) {
            selectedItemProperty.set(item)
        } else {
            if (itemsSameExtension.isNotEmpty()) {
                selectedItemProperty.set(itemsSameExtension.first())
            }
        }
    }

    override fun createCenterPanel(): JComponent = savePanel

    override fun doOKAction() {
        isCanceled = false
        super.doOKAction()
    }

    override fun doCancelAction() {
        isCanceled = true
        super.doCancelAction()
    }
}



