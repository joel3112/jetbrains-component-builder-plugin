package org.joel3112.componentbuilder.settings.data

interface SettingsState {
    val items: MutableList<Item>
    val variables: MutableList<Variable>
}
