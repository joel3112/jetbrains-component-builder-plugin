package org.joel3112.componentbuilder.ui.settingsComponent.components;

import com.intellij.util.ui.FormBuilder;
import org.joel3112.componentbuilder.settings.data.MutableState;
import org.joel3112.componentbuilder.ui.settingsComponent.Component;

import javax.swing.*;

public class NameFormComponent implements Component, MutableState<String> {
    private JTextField textField;
    private JPanel rootPanel;

    public NameFormComponent(String name) {
        textField.setText(name);
    }

    @Override
    public void addToBuilder(FormBuilder builder) {
        builder.addLabeledComponent(rootPanel, new JLabel());
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
