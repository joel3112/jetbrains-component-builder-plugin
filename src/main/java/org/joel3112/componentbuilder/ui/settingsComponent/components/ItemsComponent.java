package org.joel3112.componentbuilder.ui.settingsComponent.components;

import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.joel3112.componentbuilder.settings.data.Item;
import org.joel3112.componentbuilder.settings.data.MutableState;
import org.joel3112.componentbuilder.ui.settingsComponent.Component;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ItemsComponent implements Component, MutableState<List<Item>> {
    private final List<JBTextField> nameTextFields;

    public ItemsComponent(List<Item> items) {
        nameTextFields = new ArrayList<>();

        for (Item item : items) {
            nameTextFields.add(new JBTextField(item.getName()));
        }
    }

    @Override
    public void addToBuilder(FormBuilder formBuilder) {
        JPanel panel = new JPanel();
        for (int i = 0; i < nameTextFields.size(); i++) {
            JPanel itemPanel = new JPanel();
            itemPanel.add(new JLabel("Item:" + (i + 1)));
            itemPanel.add(nameTextFields.get(i));
            panel.add(itemPanel);
        }
        formBuilder.addLabeledComponent(panel, new JLabel());
    }

    @Override
    public List<Item> getState() {
        List<Item> values = new ArrayList<>();
        for (int i = 0; i < nameTextFields.size(); i++) {
            values.add(new Item(nameTextFields.get(i).getText()));
        }
        return values;
    }

    @Override
    public void setState(List<Item> items) {
        for (int i = 0; i < items.size(); i++) {
            nameTextFields.get(i).setText(items.get(i).getName());
        }
    }
}
