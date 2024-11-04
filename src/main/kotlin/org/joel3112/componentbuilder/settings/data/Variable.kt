package org.joel3112.componentbuilder.settings.data

import com.intellij.util.xmlb.annotations.OptionTag

data class Variable(
    @get:OptionTag("NAME")
    var name: String = "",

    @get:OptionTag("EXPRESSION")
    var expression: String = "",

    @get:OptionTag("REQUIRED")
    var required: Boolean = false,
)
