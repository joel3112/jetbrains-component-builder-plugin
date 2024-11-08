package org.joel3112.componentbuilder.settings.ui.components

import VariablesResolver
import com.intellij.CommonBundle
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ComboBoxCellEditor
import com.intellij.util.ui.EditableModel
import com.intellij.util.ui.JBUI
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.SettingsService
import org.joel3112.componentbuilder.settings.data.Variable
import java.util.*
import javax.swing.DefaultCellEditor
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableModel


class VariablesDialog(
    private val settingsProperty: GraphProperty<SettingsService>,
    templateVariables: List<String>,
    project: Project,
) : DialogWrapper(project, true) {

    private var myTable: JBTable? = null
    private val variablesProperty = settingsProperty.transform(
        { it.variables },
        {
            settingsProperty.get().apply {
                with(variables) {
                    clear()
                    addAll(it)
                }
            }
        }
    )

    private val listVariables: MutableList<Variable> =
        (listOf(VariablesResolver.defaultVariable) + templateVariables.map { name ->
            val variableInSettings = variablesProperty.get().find { it.name == name }
            Variable(name, variableInSettings?.expression ?: "", false)
        }).toMutableList()
    private val variablesAreModified: Boolean =
        listVariables.hashCode() != variablesProperty.get().hashCode()

    init {
        init()
        title = message("builder.dialog.variables.title")
        setOKButtonText(CommonBundle.getOkButtonText())
    }


    override fun getHelpId(): String = "editing.templates.defineTemplates.editTemplVars"
    override fun getPreferredFocusedComponent(): JComponent = myTable!!
    override fun createCenterPanel(): JComponent = createVariablesTable()
    override fun doOKAction() {
        if (myTable!!.isEditing) {
            val editor = myTable!!.cellEditor
            editor?.stopCellEditing()
        }

        if (variablesAreModified) {
            val currentVariables: MutableList<Variable> = variablesProperty.get()
            listVariables.forEach { variable ->
                val currentVariable = currentVariables.find { it.name == variable.name }
                if (currentVariable != null) {
                    currentVariables[currentVariables.indexOf(currentVariable)] = variable
                } else {
                    currentVariables.add(variable)
                }
            }
            variablesProperty.set(currentVariables.toMutableList())
        }
        super.doOKAction()
    }

    private fun createVariablesTable(): JComponent {
        val names = arrayOf(
            message("builder.dialog.variables.table.column.name"),
            message("builder.dialog.variables.table.column.expression"),
        )

        // Create a model of the data.
        val dataModel: TableModel = VariablesModel(names)

        // Create the table
        myTable = JBTable(dataModel)
        myTable!!.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        myTable!!.preferredScrollableViewportSize = JBUI.size(600, -1)
        myTable!!.visibleRowCount = 8
        myTable!!.getColumn(names[0]).preferredWidth = 100
        myTable!!.getColumn(names[1]).preferredWidth = 300
        if (listVariables.size > 0) {
            myTable!!.selectionModel.setSelectionInterval(0, 0)
        }

        var cellEditor: DefaultCellEditor = object : ComboBoxCellEditor() {
            override fun getComboBoxItems(): List<String> {
                return VariablesResolver.expressionList
            }

            override fun isComboboxEditable(): Boolean {
                return true
            }
        }
        cellEditor.clickCountToStart = 1
        myTable!!.getColumn(names[1]).cellEditor = cellEditor

        val textField = JTextField()
        cellEditor = DefaultCellEditor(textField)
        cellEditor.clickCountToStart = 1
        myTable!!.setDefaultEditor(String::class.java, cellEditor)

        val decorator = ToolbarDecorator.createDecorator(myTable!!).disableAddAction().disableRemoveAction()

        return panel {
            row {
                comment(message("builder.dialog.variables.description"))
            }
            row {
                cell(decorator.createPanel()).align(Align.FILL)
            }
        }
    }

    private inner class VariablesModel(private val myNames: Array<String>) : AbstractTableModel(), EditableModel {
        override fun getColumnCount(): Int = myNames.size
        override fun getRowCount(): Int = listVariables.size

        override fun getValueAt(row: Int, col: Int): String {
            val variable = listVariables[row]
            if (col == 0) {
                return variable.name
            }
            return variable.expression
        }

        override fun setValueAt(aValue: Any, row: Int, col: Int) {
            val variable = listVariables[row]
            if (col == 1) {
                variable.expression = aValue as String
            }
        }

        override fun getColumnName(column: Int): String = myNames[column]
        override fun getColumnClass(c: Int): Class<*> = String::class.java
        override fun isCellEditable(row: Int, col: Int): Boolean = col == 1 && !listVariables[row].required
        override fun addRow() = throw UnsupportedOperationException()
        override fun removeRow(index: Int) = throw UnsupportedOperationException()
        override fun exchangeRows(oldIndex: Int, newIndex: Int) = Collections.swap(listVariables, oldIndex, newIndex)
        override fun canExchangeRows(oldIndex: Int, newIndex: Int): Boolean = true
    }
}
