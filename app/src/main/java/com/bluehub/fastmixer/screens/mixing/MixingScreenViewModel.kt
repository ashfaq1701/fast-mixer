package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.FileManager
import com.bluehub.fastmixer.common.utils.PermissionManager
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import javax.inject.Inject


class MixingScreenViewModel @Inject constructor(override val context: Context,
                                                override val permissionManager: PermissionManager,
                                                val fileManager: FileManager,
                                                val mixingRepository: MixingRepository): PermissionViewModel(context) {
    var audioFiles: MutableList<AudioFile> = mutableListOf()
    val audioFilesLiveData = MutableLiveData<MutableList<AudioFile>>(mutableListOf())

    private val _eventRecord = MutableLiveData<Boolean>()
    val eventRecord: LiveData<Boolean>
        get() = _eventRecord

    private val _eventRead = MutableLiveData<Boolean>()
    val eventRead: LiveData<Boolean>
        get() = _eventRead

    private val _itemRemovedIdx = MutableLiveData<Int>()
    val itemRemovedIdx: LiveData<Int>
        get() = _itemRemovedIdx

    private val _itemAdded = MutableLiveData<Boolean>()
    val itemAdded: LiveData<Boolean>
        get() = _itemAdded

    init {
        mixingRepository.createMixingEngine()
    }

    fun onRecord() {
        _eventRecord.value = true
    }

    fun onReadFromDisk() {
        _eventRead.value = true
    }

    fun resetReadFromDisk() {
        _eventRead.value = false
    }

    fun onSaveToDisk() {

    }

    fun onRecordNavigated() {
        _eventRecord.value = false
    }

    fun setItemAdded() {
        _itemAdded.value = true
    }

    fun resetItemAdded() {
        _itemAdded.value = false
    }

    fun addRecordedFilePath(filePath: String) {
        if (!fileManager.fileExists(filePath)) return

        if (audioFiles.filter {
            it.path == filePath
        }.count() == 0) {
            audioFiles.add(AudioFile(filePath, AudioFileType.RECORDED))
            audioFilesLiveData.value = audioFiles
            setItemAdded()
        }
    }

    fun addReadFile(fileUri: Uri) {
        val fileName = fileManager.getFileNameFromUri(context.contentResolver, fileUri)
        viewModelScope.launch(Dispatchers.IO) {
            val newFilePath = fileName?.let { name ->
                val fileInputStream = context.contentResolver.openInputStream(fileUri)
                fileInputStream?.use {
                    val dir = createImportedFileCacheDirectory()
                    val importId = UUID.randomUUID().toString()

                    fileManager.copyFileFromUri(it, name, dir, importId)
                }
            }

            newFilePath?.let { newPath ->
                withContext(Dispatchers.Main) {
                    audioFiles.add(AudioFile(newPath, AudioFileType.IMPORTED))
                    audioFilesLiveData.value = audioFiles
                    setItemAdded()
                }
            }
        }
    }

    fun createImportedFileCacheDirectory(): String {
        val cacheDir = "${context.cacheDir}/imported"
        val fileObj = File(cacheDir)
        if (!fileObj.exists()) {
            fileObj.mkdir()
        }
        return cacheDir
    }

    fun addFile(filePath: String): Job = viewModelScope.launch {
        mixingRepository.addFile(filePath)
    }

    fun readSamples(filePath: String) = fun(countPoints: Int): Deferred<Array<Float>> =
        viewModelScope.async {
            mixingRepository.readSamples(filePath, countPoints)
        }


    fun deleteFile(filePath: String) {
        viewModelScope.launch {
            mixingRepository.deleteFile(filePath)

            val idxToRemove = audioFiles.foldIndexed(listOf<Int>(), { idx, list, file ->
                if (file.path == filePath) {
                    list + idx
                } else {
                    list
                }
            })

            val firstIdx = idxToRemove.firstOrNull()

            firstIdx?.let {
                val removedFile = audioFiles.removeAt(it)
                fileManager.removeFile(removedFile.path)
                audioFilesLiveData.value = audioFiles
                _itemRemovedIdx.value = it
            }
        }
    }

    fun resetItemRemovedIdx() {
        _itemRemovedIdx.value = null
    }


    fun getTotalSamples(filePath: String): Int = mixingRepository.getTotalSamples(filePath)

    override fun onCleared() {
        super.onCleared()
        mixingRepository.deleteMixingEngine()
    }
}