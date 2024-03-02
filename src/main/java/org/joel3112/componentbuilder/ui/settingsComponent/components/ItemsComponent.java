package org.joel3112.componentbuilder.ui.settingsComponent.components;

import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.joel3112.componentbuilder.settings.data.MutableState;
import org.joel3112.componentbuilder.ui.settingsComponent.Component;

import javax.swing.*;

public class ItemsComponent implements Component, MutableState<String[]> {
    private final JBTextField[] textFields;

    public ItemsComponent(String[] items) {
        textFields = new JBTextField[items.length];
        for (int i = 0; i < items.length; i++) {
            textFields[i] = new JBTextField(items[i]);
        }
    }

    @Override
    public void addToBuilder(FormBuilder formBuilder) {
        JPanel panel = new JPanel();
        for (JBTextField textField : textFields) {
            JPanel itemPanel = new JPanel();
            itemPanel.add(new JLabel("Item:"));
            itemPanel.add(textField);
            panel.add(itemPanel);
        }
        formBuilder.addLabeledComponent(panel, new JLabel());
    }

    @Override
    public String[] getState() {
        String[] values = new String[textFields.length];
        for (int i = 0; i < textFields.length; i++) {
            values[i] = textFields[i].getText();
        }
        return values;
    }

    @Override
    public void setState(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            textFields[i].setText(strings[i]);
        }
    }
}
