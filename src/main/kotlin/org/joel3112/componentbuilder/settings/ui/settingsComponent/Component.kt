package org.joel3112.componentbuilder.settings.ui.settingsComponent

import com.intellij.util.ui.FormBuilder


interface Component {
    fun addToBuilder(formBuilder: FormBuilder)
}
