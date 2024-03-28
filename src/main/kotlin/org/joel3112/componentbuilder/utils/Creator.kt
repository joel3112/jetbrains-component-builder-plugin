package org.joel3112.componentbuilder.utils

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.joel3112.componentbuilder.settings.data.Item
import java.io.File


class Creator(
    private var directory: VirtualFile,
    cname: String,
    item: Item,
) : Runnable {

    private val cTemplate = StringUtils.replaceVariables(item.template, cname)
    private val cFilePath = StringUtils.replaceVariables(item.filePath, cname)
        .replaceFirst("/", "")

    private val cRelativeFile = File(cFilePath)
    private val cFile = File(
        directory.path + "/" + cRelativeFile.path
    )


    @Throws(Exception::class)
    fun writeFile() {
        if (FileUtils.fileExists(cFile.path)) {
            println("File is already exists: ${cFile.path}")
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
            FileUtils.writeFile(
                cDirectory.createChildData(cDirectory, cFile.name),
                cTemplate
            )
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

