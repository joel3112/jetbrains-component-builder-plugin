package org.joel3112.componentbuilder.settings.data

import com.intellij.util.xmlb.annotations.OptionTag

data class Item(
    @get:OptionTag("ID")
    var id: String = "",

    @get:OptionTag("NAME")
    var name: String = ""
)

