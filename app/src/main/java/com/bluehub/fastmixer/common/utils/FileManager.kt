package com.bluehub.fastmixer.common.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.*
import javax.inject.Inject

class FileManager @Inject constructor(
    @ApplicationContext val context: Context
) {
    fun removeFile(filePath: String) {
        val file = File(filePath)
        file.delete()
    }

    fun fileExists(path: String) = File(path).exists()

    fun getFileNameFromUri(resolver: ContentResolver, uri: Uri): String? {
        val c: Cursor? = resolver.query(uri, null, null, null, null)
        return c?.use {
            it.moveToFirst()
            it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }

    fun getExtension(fileName: String) = fileName.substring(fileName.lastIndexOf("."))

    fun copyFileFromUri(inputStream: InputStream, fileName: String, dir: String, newName: String): String {
        val newPath = "$dir/$newName${getExtension(fileName)}"

        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null

        return try {
            bis = BufferedInputStream(inputStream)
            bos = BufferedOutputStream(FileOutputStream(newPath, false))

            val buf = ByteArray(1024)
            bis.read(buf)

            do {
                bos.write(buf)
            } while (bis.read(buf) != -1)

            newPath
        } finally {
            bis?.close()
            bos?.close()
        }
    }

    fun getFdForPath(path: String): ParcelFileDescriptor? {
        val file = File(path)
        return if (file.exists()) {
            val uri = Uri.fromFile(file)
            getFdForUri(uri)
        } else null
    }

    private fun getFdForUri(uri: Uri): ParcelFileDescriptor? {
        return context.contentResolver.openFileDescriptor(uri, "r")
    }
}
