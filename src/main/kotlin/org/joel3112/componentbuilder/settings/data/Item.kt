package org.joel3112.componentbuilder.settings.data

import com.intellij.util.xmlb.annotations.OptionTag
import java.util.*

data class Item(
    @get:OptionTag("ID")
    var id: String = UUID.randomUUID().toString(),

    @get:OptionTag("PARENT")
    var parent: String = "",

    @get:OptionTag("NAME")
    var name: String = "Unnamed",

    @get:OptionTag("FILE_PATH")
    var filePath: String = "",

    @get:OptionTag("TEMPLATE")
    var template: String = ""
)

