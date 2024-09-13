package org.joel3112.componentbuilder.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.joel3112.componentbuilder.BuilderBundle.message
import org.joel3112.componentbuilder.settings.data.Item
import java.io.File

class BuilderCreator(
    private var directory: VirtualFile,
    cname: String,
    item: Item,
    private val project: Project
) : Runnable {

    private val cTemplate = item.templateFormatted(cname)
    private val cFilePath = item.filePathFormatted(cname).replaceFirst("/", "")

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



