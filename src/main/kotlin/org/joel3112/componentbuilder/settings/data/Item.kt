package org.joel3112.componentbuilder.settings.data

import com.intellij.util.xmlb.annotations.OptionTag
import net.pearx.kasechange.toCamelCase
import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toPascalCase
import org.joel3112.componentbuilder.utils.toReactHookCase
import java.util.*

const val DEFAULT_NAME = "Unnamed"
val createDefaultId = { UUID.randomUUID().toString() }

data class Item(
    @get:OptionTag("ID")
    var id: String = createDefaultId(),

    @get:OptionTag("ENABLED")
    var enabled: Boolean = true,

    @get:OptionTag("PARENT")
    var parent: String = "",

    @get:OptionTag("NAME")
    var name: String = DEFAULT_NAME,

    @get:OptionTag("FILE_PATH")
    var filePath: String = "",

    @get:OptionTag("TEMPLATE")
    var template: String = "",
) {
    val isParent: Boolean
        get() = parent.isEmpty()

    private fun replaceVariables(path: String, cname: String): String {
        if (path.isEmpty()) {
            return cname
        }
        return path
            .replace("${"$"}{NAME}", cname)
            .replace("${"$"}{KEBAB_NAME}", cname.toKebabCase())
            .replace("${"$"}{PASCAL_NAME}", cname.toPascalCase())
            .replace("${"$"}{CAMEL_NAME}", cname.toCamelCase())
            .replace("${"$"}{REACT_HOOK_NAME}", cname.toReactHookCase())
    }

    fun filePathFormatted(cname: String): String {
        return replaceVariables(filePath, cname)
    }

    fun templateFormatted(cname: String): String {
        return replaceVariables(template, cname)
    }
}
