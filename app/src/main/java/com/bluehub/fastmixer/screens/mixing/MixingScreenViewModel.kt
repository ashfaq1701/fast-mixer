package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.FileManager
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MixingScreenViewModel @Inject constructor (override val context: Context,
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

    init {
        mixingRepository.createMixingEngine()
    }

    fun onRecord() {
        _eventRecord.value = true
    }

    fun onReadFromDisk() {
        if(!checkReadFilePermission()) {
            setRequestReadFilePermission(ScreenConstants.READ_FROM_FILE)
            return
        }
    }

    fun onSaveToDisk() {
        if(!checkWriteFilePermission()) {
            setRequestWriteFilePermission(ScreenConstants.WRITE_TO_FILE)
            return
        }
    }

    fun onRecordNavigated() {
        _eventRecord.value = false
    }

    fun addRecordedFilePath(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            if (audioFiles.filter {
                it.path == filePath
            }.count() == 0) {
                audioFiles.add(AudioFile(filePath, AudioFileType.RECORDED))
                audioFilesLiveData.value = audioFiles
            }
        }
    }

    fun addFile(filePath: String): Job = viewModelScope.launch {
        mixingRepository.addFile(filePath)
    }

    fun readSamples(filePath: String) = fun (countPoints: Int): Deferred<Array<Float>> =
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