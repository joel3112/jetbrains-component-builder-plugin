package org.joel3112.componentbuilder.settings.data

import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.OptionTag

class SettingsState : BaseState() {
    @get:OptionTag("ITEMS")
    var items by list<Item>()
}
