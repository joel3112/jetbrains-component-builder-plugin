package org.joel3112.componentbuilder.ui.settingsComponent.components;

import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.joel3112.componentbuilder.settings.data.MutableState;
import org.joel3112.componentbuilder.ui.settingsComponent.Component;

import javax.swing.*;

public class NameComponent implements Component, MutableState<String> {
    private final JBTextField textField;

    public NameComponent(String name) {
        textField = new JBTextField(name);
    }

    @Override
    public void addToBuilder(FormBuilder builder) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Name:"));
        panel.add(textField);
        builder.addLabeledComponent(panel, new JLabel());
    }

    @Override
    public String getState() {
        return textField.getText();
    }

    @Override
    public void setState(String state) {
        textField.setText(state);
    }
}
