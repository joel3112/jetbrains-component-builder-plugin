package org.joel3112.componentbuilder.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import net.pearx.kasechange.toCamelCase
import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toPascalCase
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import java.io.File


class BuilderCreator(
    private var directory: VirtualFile,
    cname: String,
    item: Item,
    private val project: Project
) : Runnable {

    private val cTemplate = item.template.replaceVariables(cname)
    private val cFilePath = item.filePath.replaceVariables(cname).replaceFirst("/", "")

    private val cRelativeFile = File(cFilePath)
    private val cFile = File(
        directory.path + "/" + cRelativeFile.path
    )


    @Throws(Exception::class)
    fun writeFile() {
        if (FileUtils.fileExists(cFile.path)) {
            NotificationUtils.notifyError(
                message("builder.notification.create.error", cFilePath, directory.path),
                project
            )
            return
        }

        var cDirectory = directory
        if (cRelativeFile.parent != null) {
            cDirectory = VfsUtil.createDirectoryIfMissing(
                directory,
                cRelativeFile.parent
            )!!
        }

        try {
            val cVirtualFile = cDirectory.createChildData(cDirectory, cFile.name)

            FileUtils.writeFile(cVirtualFile, cTemplate)
            FileUtils.openFile(cVirtualFile, project)

            NotificationUtils.notifyInfo(message("builder.notification.create.success", cFilePath), project)

        } catch (e: Exception) {
            throw Exception("" + (e.message) + " for file " + cDirectory, e.fillInStackTrace())
        }
    }

    override fun run() {
        try {
            writeFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun String.replaceVariables(cname: String): String {
    if (this.isEmpty()) {
        return cname
    }
    return this
        .replace("${"$"}NAME${"$"}", cname)
        .replace("${"$"}KEBAB_NAME${"$"}", cname.toKebabCase())
        .replace("${"$"}PASCAL_NAME${"$"}", cname.toPascalCase())
        .replace("${"$"}CAMEL_NAME${"$"}", cname.toCamelCase())

}

