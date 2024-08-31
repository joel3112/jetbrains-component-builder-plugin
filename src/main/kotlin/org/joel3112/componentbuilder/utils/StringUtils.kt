package org.joel3112.componentbuilder.utils

import net.pearx.kasechange.toCamelCase
import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toPascalCase

fun String.replaceVariables(cname: String): String {
    if (this.isEmpty()) {
        return cname
    }
    return this
        .replace("${"$"}NAME${"$"}", cname)
        .replace("${"$"}KEBAB_NAME${"$"}", cname.toKebabCase())
        .replace("${"$"}PASCAL_NAME${"$"}", cname.toPascalCase())
        .replace("${"$"}CAMEL_NAME${"$"}", cname.toCamelCase())

}
