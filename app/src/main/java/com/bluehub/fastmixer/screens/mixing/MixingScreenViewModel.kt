package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

class MixingScreenViewModel @Inject constructor (override val context: Context,
                                                 override val permissionManager: PermissionManager,
                                                 val mixingRepository: MixingRepository): PermissionViewModel(context) {
    var audioFiles: MutableList<AudioFile> = mutableListOf()
    val audioFilesLiveData = MutableLiveData<MutableList<AudioFile>>(mutableListOf())

    private val _eventRecord = MutableLiveData<Boolean>()
    val eventRecord: LiveData<Boolean>
        get() = _eventRecord

    private val _eventRead = MutableLiveData<Boolean>()
    val eventRead: LiveData<Boolean>
        get() = _eventRead

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
            }.count() > 0) {
                audioFiles.add(AudioFile(filePath, AudioFileType.RECORDED))
                audioFilesLiveData.value = audioFiles
            }
        }
    }

    fun addFile(filePath: String): Job = viewModelScope.launch {
        mixingRepository.addFile(filePath)
    }

    fun readSamples(filePath: String) = fun (numSamples: Int): Array<Float> = runBlocking(Dispatchers.IO) {
        mixingRepository.readSamples(filePath, numSamples)
    }


    fun deleteFile(filePath: String) {
        viewModelScope.launch {
            mixingRepository.deleteFile(filePath)
            audioFiles.remove(
                audioFiles.find {
                    it.path == filePath
                }
            )

            audioFilesLiveData.value = audioFiles
        }
    }


    fun getTotalSamples(filePath: String): Int = mixingRepository.getTotalSamples(filePath)

    override fun onCleared() {
        super.onCleared()
        mixingRepository.deleteMixingEngine()
    }
}