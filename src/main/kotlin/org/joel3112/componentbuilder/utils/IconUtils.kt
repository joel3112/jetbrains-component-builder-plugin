package org.joel3112.componentbuilder.utils

import com.intellij.openapi.util.IconLoader
import org.joel3112.componentbuilder.settings.data.Item
import javax.swing.Icon
import javax.swing.UIManager

private data class FileIcon(
    val fileType: String,
    val lightIconPath: String,
    val darkIconPath: String
)

class IconUtils {
    companion object {
        private val mapFileNameToIcon = mapOf(
            "default" to FileIcon("AnyType", "/icons/files/anyType.svg", "/icons/files/anyType_dark.svg"),
            ".*\\.astro" to FileIcon("Astro", "/icons/files/astro.svg", "/icons/files/astro_dark.svg"),
            ".*\\.css" to FileIcon("Css", "/icons/files/css.svg", "/icons/files/css_dark.svg"),
            ".*\\.html" to FileIcon("Html", "/icons/files/html.svg", "/icons/files/html_dark.svg"),
            ".*\\.(test|spec)\\.js" to FileIcon("JsTest", "/icons/files/jsTest.svg", "/icons/files/jsTest_dark.svg"),
            ".*\\.js" to FileIcon("JavaScript", "/icons/files/javaScript.svg", "/icons/files/javaScript_dark.svg"),
            ".*\\.json" to FileIcon("Json", "/icons/files/json.svg", "/icons/files/json_dark.svg"),
            ".*\\.md" to FileIcon("Markdown", "/icons/files/markdown.svg", "/icons/files/markdown_dark.svg"),
            ".*\\.mdx" to FileIcon("Mdx", "/icons/files/mdx.svg", "/icons/files/mdx_dark.svg"),
            ".*\\.sass" to FileIcon("Sass", "/icons/files/sass.svg", "/icons/files/sass_dark.svg"),
            ".*\\.scss" to FileIcon("Scss", "/icons/files/sass.svg", "/icons/files/sass_dark.svg"),
            ".*\\.svelte" to FileIcon("Svelte", "/icons/files/svelte.svg", "/icons/files/svelte_dark.svg"),
            ".*\\.txt" to FileIcon("Text", "/icons/files/text.svg", "/icons/files/text_dark.svg"),
            ".*\\.(test|spec)\\.ts" to FileIcon(
                "TypeScriptTest",
                "/icons/files/tsTest.svg",
                "/icons/files/tsTest_dark.svg"
            ),
            ".*\\.ts" to FileIcon("TypeScript", "/icons/files/typeScript.svg", "/icons/files/typeScript_dark.svg"),
            ".*\\.(test|spec)\\.(jsx|tsx)" to FileIcon(
                "ReactTest",
                "/icons/files/tsxTest.svg",
                "/icons/files/tsxTest_dark.svg"
            ),
            ".*\\.(jsx|tsx)" to FileIcon("React", "/icons/files/tsx.svg", "/icons/files/tsx_dark.svg"),
            ".*\\.vue" to FileIcon("VueJs", "/icons/files/vue.svg", "/icons/files/vue_dark.svg"),
            ".*\\.xml" to FileIcon("Xml", "/icons/files/xml.svg", "/icons/files/xml_dark.svg"),
            ".*\\.yaml" to FileIcon("Yaml", "/icons/files/yaml.svg", "/icons/files/yaml_dark.svg"),
        )


        private fun isDarkMode(): Boolean {
            val lafName = UIManager.getLookAndFeel().name
            return lafName.contains("Darcula", ignoreCase = true) ||
                    lafName.contains("Dark", ignoreCase = true)
        }

        private fun generateIcon(iconPath: String): Icon {
            return IconLoader.getIcon(iconPath, this::class.java)
        }

        private fun getIconMatchFilePath(path: String): Pair<String, Icon> {
            val darkMode = isDarkMode()
            for ((pattern, fileIcon) in mapFileNameToIcon) {
                if (path.matches(Regex(pattern))) {
                    val iconPath = if (darkMode) fileIcon.darkIconPath else fileIcon.lightIconPath
                    val icon = generateIcon(iconPath)
                    return Pair(fileIcon.fileType, icon)
                }
            }
            val defaultIcon = mapFileNameToIcon["default"]
            val defaultIconPath = if (darkMode) defaultIcon?.darkIconPath else defaultIcon?.lightIconPath
            val icon = generateIcon(defaultIconPath ?: "")
            return Pair(defaultIcon?.fileType ?: "Any", icon)
        }

        fun getIconByItem(item: Item): Pair<String, Icon> {
            val filePath = FileUtils.getFileName(item.filePathFormatted(item.name))
            return getIconMatchFilePath(filePath)
        }
    }

}
