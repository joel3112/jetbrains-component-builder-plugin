package org.joel3112.componentbuilder.utils

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
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

        fun openFile(file: VirtualFile, project: Project) {
            val editorManager = FileEditorManager.getInstance(project)
            editorManager.openFile(file, true)
        }

        fun getFileName(filePath: String): String {
            val fileName = File(filePath).name
            return fileName
        }

        fun getFileExtension(filePath: String): String {
            val extension = File(filePath).extension
            return extension
        }
    }
}

