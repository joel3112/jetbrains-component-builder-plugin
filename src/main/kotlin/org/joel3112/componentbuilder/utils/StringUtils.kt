package org.joel3112.componentbuilder.utils

import net.pearx.kasechange.toPascalCase
import java.util.regex.Pattern

fun String.toReactHookCase(): String {
    if (this.startsWith("use")) {
        return this
    }
    return "use${this.toPascalCase()}"
}


fun String.convertRegexToPath(): String {
    // Manually replace escaped slashes and dots in the regex
    val cleanedRegex = this
        .replace("\\/", "/")   // Replace escaped slashes with normal slashes
        .replace("\\.", ".")   // Replace escaped dots with normal dots

    // This pattern will match full directory names and filenames with extensions
    val pattern = Pattern.compile("[\\w-]+(?:\\.[a-z]+)?")
    val matcher = pattern.matcher(cleanedRegex)
    val pathParts = mutableListOf<String>()

    // Collect valid components from the cleaned regex string
    while (matcher.find()) {
        pathParts.add(matcher.group())  // Add directory names and filenames with extensions
    }

    // Check if the regex contains directories (slashes) or just a file
    val hasDirectory = this.contains("/")

    // Join the valid parts with slashes; if there's a directory, add the leading "/"
    return if (hasDirectory && pathParts.size > 1) {
        "/" + pathParts.joinToString("/")
    } else {
        pathParts.joinToString("/")
    }
}
