package org.joel3112.componentbuilder.utils

import com.intellij.openapi.vfs.VirtualFile
import java.io.*
import java.util.*

class FileUtils {
    companion object {
        @Throws(IOException::class)
        fun writeFile(destinationFile: VirtualFile, content: String) {
            destinationFile.setBinaryContent(content.toByteArray())
        }

        fun fileExists(filePath: String): Boolean {
            val tmpDir = File(filePath)
            return tmpDir.exists()
        }

        fun getFileNameFromPath(filePath: String): String {
            val file = File(filePath)
            return file.name
        }

        fun getDirectoryFromPath(filePath: String?): String {
            val file = File(filePath)
            if (file.parentFile == null) {
                return ""
            }
            return file.parent
        }
    }
}

