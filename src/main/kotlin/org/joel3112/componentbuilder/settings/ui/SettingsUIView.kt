package org.joel3112.componentbuilder.settings.ui

import org.joel3112.componentbuilder.settings.data.MutableState
import org.joel3112.componentbuilder.settings.data.SettingsState
import javax.swing.JComponent

interface SettingsUIView : MutableState<SettingsState> {

    fun createComponent(): JComponent
}

