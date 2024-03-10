package org.joel3112.componentbuilder.ui.settingsComponent.components;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import org.joel3112.componentbuilder.settings.data.Item;
import org.joel3112.componentbuilder.settings.data.MutableState;
import org.joel3112.componentbuilder.ui.settingsComponent.Component;

import javax.swing.*;
import java.util.List;

public class ListSelectorComponent implements Component, MutableState<List<Item>> {

    private List<Item> listItems;
    private final JList<String> list = new JBList<>();

    public ListSelectorComponent(List<Item> items) {
        listItems = items;
        list.setListData(items.stream().map(Item::getName).toArray(String[]::new));
        list.setSelectedIndex(0);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel itemName = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                itemName.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return itemName;
            }
        });
    }

    public Item getSelectedItem() {
        return listItems.get(list.getSelectedIndex());
    }

    @Override
    public void addToBuilder(FormBuilder builder) {
        JPanel panel = new JPanel();
        panel.add(list);
        builder.addLabeledComponent(panel, new JLabel());
    }

    @Override
    public List<Item> getState() {
        return this.listItems;
    }

    @Override
    public void setState(List<Item> items) {
        this.listItems = items;
        list.setListData(items.stream().map(Item::getName).toArray(String[]::new));
    }
}
