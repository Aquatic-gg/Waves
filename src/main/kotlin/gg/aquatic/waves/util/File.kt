package gg.aquatic.waves.util

import java.io.File

fun File.filesLookup(filter: (File) -> Boolean) =
    this.listFiles()?.filter { f -> !f.isDirectory && filter(f)}?.toList() ?: emptyList()

fun File.deepFilesLookup(filter: (File) -> Boolean): List<File> {
    val files = ArrayList<File>()

    fun lookup(folder: File) {
        folder.listFiles()?.forEach {
            if (it.isDirectory) {
                lookup(it)
            } else if (filter(it)) {
                files.add(it)
            }
        }
    }

    for (file in listFiles() ?: emptyArray()) {
        if (file.isDirectory) {
            lookup(file)
            continue
        } else if (filter(file)) {
            files.add(file)
        }
    }
    return files
}