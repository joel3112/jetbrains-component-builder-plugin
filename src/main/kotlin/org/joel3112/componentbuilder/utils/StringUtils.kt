package org.joel3112.componentbuilder.utils

import net.pearx.kasechange.toPascalCase

fun String.toReactHookCase(): String {
    if (this.startsWith("use")) {
        return this
    }
    return "use${this.toPascalCase()}"
}

