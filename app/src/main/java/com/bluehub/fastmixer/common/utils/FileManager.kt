package com.bluehub.fastmixer.common.utils

import java.io.File
import javax.inject.Inject

class FileManager @Inject constructor() {
    fun removeFile(filePath: String) {
        val file = File(filePath)
        file.delete()
    }
}