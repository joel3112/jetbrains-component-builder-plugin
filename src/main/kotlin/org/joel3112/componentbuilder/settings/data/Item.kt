package org.joel3112.componentbuilder.settings.data

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.util.xmlb.annotations.OptionTag
import org.joel3112.componentbuilder.utils.FileUtils
import org.joel3112.componentbuilder.utils.replaceName
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

    @get:OptionTag("REGEX_MATCH")
    var regexMatch: String = "",

    @get:OptionTag("TEMPLATE")
    var template: String = "",
) {
    val isParent: Boolean
        get() = parent.isEmpty()

    val language: Language?
        get() {
            val fileExtension = FileUtils.getFileExtension(filePath.replaceName(name))
            val fileType = FileTypeManager.getInstance().getFileTypeByExtension(fileExtension)

            return if (fileType is LanguageFileType) {
                fileType.language
            } else {
                null
            }
        }

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other.hashCode()
    }

    fun filePathFormatted(cname: String, variables: MutableList<Variable>): String {
        return filePath.replaceVariables(cname, variables)
    }

    fun templateFormatted(cname: String, variables: MutableList<Variable>): String {
        return template.replaceVariables(cname, variables)
    }
}


fun MutableList<Item>.sortByParent(): MutableList<Item> {
    val orderedList = mutableListOf<Item>()
    val parentItems = this.filter { it.isParent }

    for (parent in parentItems) {
        orderedList.add(parent)
        val children = this.filter { it.parent == parent.id }
        orderedList.addAll(children)
    }

    return orderedList
}
