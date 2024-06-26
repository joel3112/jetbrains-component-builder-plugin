package org.joel3112.componentbuilder.settings.data

import com.intellij.util.xmlb.annotations.OptionTag
import java.util.*

data class Item(
    @get:OptionTag("ID")
    var id: String = UUID.randomUUID().toString(),

    @get:OptionTag("IS_CHILD_FILE")
    var isChildFile: Boolean = false,

    @get:OptionTag("PARENT_EXTENSIONS")
    var parentExtensions: String = "",

    @get:OptionTag("NAME")
    var name: String = "Unnamed",

    @get:OptionTag("ICON")
    var icon: String = "",

    @get:OptionTag("FILE_PATH")
    var filePath: String = "",

    @get:OptionTag("TEMPLATE")
    var template: String = ""
)

