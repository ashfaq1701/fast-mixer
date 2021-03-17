package com.bluehub.fastmixer.common.utils

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
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

    fun getReadOnlyFdForPath(path: String): ParcelFileDescriptor? {
        val file = File(path)
        return if (file.exists()) {
            val uri = Uri.fromFile(file)
            getReadOnlyFdForUri(uri)
        } else null
    }

    private fun getReadOnlyFdForUri(uri: Uri): ParcelFileDescriptor? {
        return context.contentResolver.openFileDescriptor(uri, "r")
    }

    fun getFileDescriptorForMedia(fileName: String): ParcelFileDescriptor? {
        val resolver = context.contentResolver

        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val newAudioDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension("wav"))
        }

        return resolver
            .insert(audioCollection, newAudioDetails)?.let {
                resolver.openFileDescriptor(it, "rw")
            }
    }
}
