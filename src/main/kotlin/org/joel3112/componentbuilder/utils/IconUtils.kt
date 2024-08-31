package org.joel3112.componentbuilder.utils

import com.intellij.icons.ExpUiIcons.FileTypes
import com.intellij.icons.ExpUiIcons.General
import javax.swing.Icon

class IconUtils {
    companion object {
        private val mapExtensionToIcon = mapOf(
            "txt" to "Text",
            "js" to "JavaScript",
//            "ts" to "typescript",
            "tsx" to "React",
            "jsx" to "React",
            "vue" to "VueJs",
            "html" to "Html",
            "css" to "Css",
            "scss" to "Scss",
            "sass" to "Scss",
            "less" to "Css",
            "json" to "Json",
            "xml" to "Cml",
            "yml" to "Yaml",
            "yaml" to "Yaml",
//            "md" to "markdown",
//            "markdown" to "markdown",
        )

        fun getIconList(): List<String> {
            val allIconsList =
                FileTypes::class.java.fields.map { it.name } + General::class.java.getField("ListFiles").name
            return allIconsList
        }

        fun getIconByValue(value: String): Icon {
            return try {
                FileTypes::class.java.getField(value).get(null) as Icon
            } catch (e: Exception) {
                General.ListFiles
            }
        }

        fun getIconValueByExtension(extension: String, isParent: Boolean): String {
            return try {
                mapExtensionToIcon[extension] as String
            } catch (e: Exception) {
                if (isParent) {
                    "ListFiles"
                } else {
                    "AnyType"
                }
            }
        }

        fun getIconByExtension(extension: String, isParent: Boolean): Icon {
            return try {
                mapExtensionToIcon[extension]?.let { FileTypes::class.java.getField(it).get(null) } as Icon
            } catch (e: Exception) {
                if (isParent) {
                    General.ListFiles
                } else {
                    FileTypes.AnyType
                }
            }
        }
    }

}
