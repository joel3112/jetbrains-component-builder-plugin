package org.joel3112.componentbuilder.settings.data

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.util.xmlb.annotations.OptionTag
import org.joel3112.componentbuilder.utils.FileUtils
import org.joel3112.componentbuilder.utils.convertRegexToPath
import org.joel3112.componentbuilder.utils.replaceVariables
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

    val language: Language?
        get() {
            val fileExtension = FileUtils.getFileExtension(filePathFormatted(name))
            val fileType = FileTypeManager.getInstance().getFileTypeByExtension(fileExtension)

            return if (fileType is LanguageFileType) {
                fileType.language
            } else {
                null
            }
        }

    fun filePathFormatted(cname: String): String {
        if (isParent) {
            return filePath.replaceVariables(cname).convertRegexToPath()
        }
        return filePath.replaceVariables(cname)
    }

    fun regexPathFormatted(cname: String): String? {
        if (isParent) {
            return filePath.replaceVariables(cname)
        }
        return null
    }

    fun templateFormatted(cname: String): String {
        return template.replaceVariables(cname)
    }
}
